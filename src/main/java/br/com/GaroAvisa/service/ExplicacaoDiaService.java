package br.com.GaroAvisa.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.GaroAvisa.dto.AlertaResponse;
import br.com.GaroAvisa.dto.ExplicacaoResponse;
import br.com.GaroAvisa.evento.PrevisaoAtualizadaEvento;
import br.com.GaroAvisa.evento.PrevisaoAtualizadaEvento.Dia;
import br.com.GaroAvisa.evento.PrevisaoAtualizadaEvento.Hora;
import br.com.GaroAvisa.model.ExplicacaoDia;
import br.com.GaroAvisa.repository.ExplicacaoDiaRepository;
import lombok.extern.slf4j.Slf4j;

// Feature 23: o texto do dia, gerado por IA uma vez e reaproveitado.
@Slf4j
@Service
public class ExplicacaoDiaService {

	private static final int TAMANHO_MAXIMO = 1000;

	private final ExplicacaoDiaRepository repository;

	// Pode ser nulo: sem a chave de API nao ha cliente, e a aplicacao PRECISA
	// subir assim mesmo. Nesse caso todo mundo cai no texto padrao
	private final ChatClient chatClient;

	public ExplicacaoDiaService(ExplicacaoDiaRepository repository,@Value("${spring.ai.openai.api-key:}") String chave, ObjectProvider<ChatClient.Builder> construtor) {
		this.repository = repository;
		this.chatClient = montarCliente(construtor);

		if (this.chatClient == null) {
			log.warn("Sem cliente de IA configurado: a Explicacao do Dia vai usar o texto padrao");
		}
	}

// Sem a chave, a auto-configuracao do Spring AI CRIA o bean e ele estoura
// ao nascer: "At least one credential source must be specified".
// O getIfAvailable so engole bean inexistente, nao bean que falha na
// construcao - dai o try/catch. A aplicacao PRECISA subir sem chave,
// caindo no texto padrao.
	private ChatClient montarCliente(ObjectProvider<ChatClient.Builder> construtor) {
		try {
			ChatClient.Builder builder = construtor.getIfAvailable();
			return builder == null ? null : builder.build();
		} catch (Exception e) {
			log.warn("Cliente de IA indisponivel: {}", e.getMessage());
			return null;
		}
	}

	// ---------- Chaves do cache ----------

	// Home: por usuario, local e dia
	public static String chaveDaHome(Long usuarioId, Long localId, LocalDate dia) {
		return "u:" + usuarioId + "|l:" + localId + "|d:" + dia;
	}

	// Pesquisa: por coordenada arredondada em 2 casas (cerca de 1 km) e dia.
	// Assim duas pessoas que pesquisam o mesmo lugar no mesmo dia dividem o texto
	public static String chaveDaPesquisa(BigDecimal latitude, BigDecimal longitude, LocalDate dia) {
		return "c:" + arredondar(latitude) + "," + arredondar(longitude) + "|d:" + dia;
	}

	private static String arredondar(BigDecimal valor) {
		return valor.setScale(2, RoundingMode.HALF_UP).toPlainString();
	}

	// ---------- Leitura sem gerar ----------

	/**
	 * So olha o cache. E o que a Home usa: se o texto ja existe ele vem junto na
	 * resposta; se nao existe, a Home devolve null e o app pede no GET /explicacao.
	 * Assim a Home NUNCA espera a IA.
	 */
	@Transactional(readOnly = true)
	public String doCache(String chave) {
		return repository.findByChave(chave).map(ExplicacaoDia::getTexto).orElse(null);
	}

	// ---------- Leitura com geracao ----------

	/**
	 * Devolve o texto do cache ou, se nao houver, gera e guarda. Se a IA falhar ou
	 * nao estiver configurada, devolve o texto padrao SEM guardar: uma falha
	 * momentanea nao pode congelar o texto do dia.
	 */
	@Transactional
	public ExplicacaoResponse obter(String chave, Long usuarioId, LocalDate dia, String lugar, String perfilIa,
			Dia hoje, List<Hora> horas, List<AlertaResponse> alertas) {

		String guardado = doCache(chave);
		if (guardado != null) {
			return new ExplicacaoResponse(guardado, true);
		}

		if (hoje == null) {
			return new ExplicacaoResponse(ExplicacaoPadrao.de(null, alertas), false);
		}

		String daIa = gerarComIa(lugar, dia, hoje, horas, alertas, perfilIa);
		if (daIa == null) {
			return new ExplicacaoResponse(ExplicacaoPadrao.de(hoje, alertas), false);
		}

		guardar(chave, usuarioId, dia, daIa);
		return new ExplicacaoResponse(daIa, true);
	}

	// ---------- Extracao dos dados do dia ----------
	// Publicos e estaticos porque a Home e a Pesquisa precisam dos mesmos dois

	public static Dia diaDe(PrevisaoAtualizadaEvento dados, LocalDate dia) {
		if (dados == null) {
			return null;
		}
		return dados.dias().stream().filter(d -> dia.equals(d.data())).findFirst().orElse(null);
	}

	public static List<Hora> horasDe(PrevisaoAtualizadaEvento dados, LocalDate dia) {
		if (dados == null) {
			return List.of();
		}
		return dados.horas().stream().filter(hora -> dia.equals(hora.horario().toLocalDate())).toList();
	}

	// ---------- IA ----------

	private String gerarComIa(String lugar, LocalDate dia, Dia hoje, List<Hora> horas, List<AlertaResponse> alertas,
			String perfilIa) {
		if (chatClient == null) {
			return null;
		}
		try {
			String texto = chatClient.prompt().system(PromptExplicacao.SISTEMA)
					.user(PromptExplicacao.dados(lugar, dia, hoje, horas, alertas, perfilIa)).call().content();

			if (texto == null || texto.isBlank()) {
				log.warn("A IA devolveu um texto vazio para a chave do dia {}", dia);
				return null;
			}
			texto = texto.trim();
			return texto.length() > TAMANHO_MAXIMO ? texto.substring(0, TAMANHO_MAXIMO) : texto;

		} catch (Exception e) {
			log.warn("Falha ao gerar a Explicacao do Dia: {}", e.getMessage());
			return null;
		}
	}

	// Duas requisicoes simultaneas podem gerar ao mesmo tempo e tentar gravar
	// a mesma chave. A unica da tabela barra a segunda - e tudo bem, porque o
	// texto dela ja foi devolvido a quem pediu
	private void guardar(String chave, Long usuarioId, LocalDate dia, String texto) {
		try {
			repository.save(new ExplicacaoDia(chave, usuarioId, dia, texto, Instant.now()));
		} catch (Exception e) {
			log.info("Explicacao do dia {} ja havia sido guardada por outra chamada", dia);
		}
	}
}