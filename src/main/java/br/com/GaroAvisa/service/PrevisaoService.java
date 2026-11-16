package br.com.GaroAvisa.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.GaroAvisa.cliente.ClimaClient;
import br.com.GaroAvisa.dto.AlertaResponse;
import br.com.GaroAvisa.dto.MudancaResponse;
import br.com.GaroAvisa.dto.PesquisaResponse;
import br.com.GaroAvisa.dto.PrevisaoResponse;
import br.com.GaroAvisa.dto.PrevisaoResponse.ClimaAtual;
import br.com.GaroAvisa.dto.PrevisaoResponse.DiaResponse;
import br.com.GaroAvisa.dto.PrevisaoResponse.HoraResponse;
import br.com.GaroAvisa.dto.PrevisaoResponse.LocalResumo;
import br.com.GaroAvisa.dto.PrevisaoResponse.Medida;
import br.com.GaroAvisa.dto.PrevisaoResponse.Sol;
import br.com.GaroAvisa.dto.PrevisaoResponse.Vento;
import br.com.GaroAvisa.evento.PrevisaoAtualizadaEvento;
import br.com.GaroAvisa.evento.PrevisaoAtualizadaEvento.Agora;
import br.com.GaroAvisa.evento.PrevisaoAtualizadaEvento.Dia;
import br.com.GaroAvisa.exception.CoordenadaInvalidaException;
import br.com.GaroAvisa.exception.FusoInvalidoException;
import br.com.GaroAvisa.exception.RecursoNaoEncontradoException;
import br.com.GaroAvisa.model.CondicaoWmo;
import br.com.GaroAvisa.model.Endereco;
import br.com.GaroAvisa.model.Local;
import br.com.GaroAvisa.model.Previsao;
import br.com.GaroAvisa.model.Uf;
import br.com.GaroAvisa.repository.LocalRepository;
import br.com.GaroAvisa.repository.MudancaPrevisaoRepository;
import br.com.GaroAvisa.repository.PrevisaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrevisaoService {

    private static final int HORAS = 48;
    private static final int DIAS_CONFIAVEIS = 7; // a partir do 8o dia a previsao e menos certeira

    private static final String[] DIRECOES = { "N", "NE", "L", "SE", "S", "SO", "O", "NO" };
    
    private static final int HORAS_PESQUISA = 24;
    private static final BigDecimal LATITUDE_MINIMA = new BigDecimal("-90");
    private static final BigDecimal LATITUDE_MAXIMA = new BigDecimal("90");
    private static final BigDecimal LONGITUDE_MINIMA = new BigDecimal("-180");
    private static final BigDecimal LONGITUDE_MAXIMA = new BigDecimal("180");

    private final PrevisaoRepository previsaoRepository;
    private final LocalRepository localRepository;
    private final ObjectMapper objectMapper;
    private final DetectorMudancaService detector;
    private final MudancaPrevisaoRepository mudancaRepository;
    private final ClimaClient climaClient;
    private final AlertaClimaService alertaService;
    

 // ---------- Consumidor do evento PrevisaoAtualizada ----------

    @Transactional
    public void salvar(PrevisaoAtualizadaEvento evento) {
        String json = paraJson(evento);

        Previsao previsao = previsaoRepository.findById(evento.enderecoId()).orElse(null);

        // Primeira previsao do endereco: gera os alertas, mas nao ha com que comparar
        if (previsao == null) {
            previsaoRepository.save(new Previsao(evento.enderecoId(), json, evento.atualizadoEm()));
            alertaService.gerar(evento);
            return;
        }

        if (!previsao.getAtualizadoEm().isBefore(evento.atualizadoEm())) {
            log.info("Previsao do endereco {} ignorada: a guardada e igual ou mais nova", evento.enderecoId());
            return;
        }

        // Feature 80 ANTES da 21: o detector precisa saber quais alertas nasceram agora
        Set<LocalDate> diasComAlertaNovo = alertaService.gerar(evento);

        // Feature 21: compara ANTES de substituir, enquanto as duas versoes existem
        detector.detectar(deJson(previsao.getDados()), evento, diasComAlertaNovo);

        previsao.atualizar(json, evento.atualizadoEm());
    }
    
    
    // ---------- GET /locais/{id}/previsao ----------

    @Transactional(readOnly = true)
    public PrevisaoResponse buscarDoLocal(Long usuarioId, Long localId) {
        Local local = localRepository.findByIdAndUsuarioId(usuarioId, localId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Local não encontrado"));

        Endereco endereco = local.getEndereco();
        LocalResumo resumo = new LocalResumo(
                local.getId(),
                local.getApelido(),
                endereco.getNome(),
                endereco.getCidade(),
                endereco.getEstado(),
                Uf.sigla(endereco.getEstado()),
                endereco.getFuso());

        Previsao previsao = previsaoRepository.findById(endereco.getId()).orElse(null);

        // Local recem-criado: a previsao ainda esta sendo buscada
        if (previsao == null) {
            return new PrevisaoResponse(resumo, null, null, List.of(), List.of(), List.of(), List.of());
        }

        PrevisaoAtualizadaEvento dados = deJson(previsao.getDados());
        
        LocalDate hoje = LocalDate.now(ZoneId.of(endereco.getFuso()));

        // As mudancas sao do ENDERECO; o Local entra so para o card mostrar o apelido
        List<MudancaResponse> mudancas = mudancaRepository
                .buscarDosEnderecos(List.of(endereco.getId()), hoje)
                .stream()
                .map(m -> MudancaResponse.de(m, local))
                .toList();

        // Feature 15: alertas de hoje E dos proximos dias
        List<AlertaResponse> alertas = alertaService.buscarDoEndereco(endereco.getId(), hoje);

        return new PrevisaoResponse(
                resumo,
                previsao.getAtualizadoEm(),
                montarClimaAtual(dados),
                montarHoras(dados),
                montarDias(dados),
                alertas,
                mudancas);
    }
    
    
    // SEM @Transactional de proposito: a chamada ao Servico de Clima pode levar
    // dezenas de segundos (o Render pode estar acordando) e nao queremos segurar
    // uma conexao do Oracle parada esse tempo todo.
    public PesquisaResponse buscarDeCoordenadas(BigDecimal latitude, BigDecimal longitude, String fuso) {
        validarCoordenadas(latitude, longitude);
        String fusoValidado = validarFuso(fuso);

        PrevisaoAtualizadaEvento dados = climaClient.buscar(latitude, longitude, fusoValidado);

        return new PesquisaResponse(
                dados.atualizadoEm(),
                montarClimaAtual(dados),
                montarHoras(dados).stream().limit(HORAS_PESQUISA).toList(),
                montarDias(dados),
                null,
                alertaService.avaliarDeHoje(dados));
    }

    private void validarCoordenadas(BigDecimal latitude, BigDecimal longitude) {
        boolean latitudeFora = latitude.compareTo(LATITUDE_MINIMA) < 0
                || latitude.compareTo(LATITUDE_MAXIMA) > 0;
        boolean longitudeFora = longitude.compareTo(LONGITUDE_MINIMA) < 0
                || longitude.compareTo(LONGITUDE_MAXIMA) > 0;

        if (latitudeFora || longitudeFora) {
            throw new CoordenadaInvalidaException();
        }
    }

    private String validarFuso(String fuso) {
        try {
            return ZoneId.of(fuso).getId();
        } catch (Exception e) {
            throw new FusoInvalidoException(fuso);
        }
    }
    	
    
    // ---------- Montagem da resposta ----------
    private ClimaAtual montarClimaAtual(PrevisaoAtualizadaEvento dados) {
        Agora agora = dados.agora();
        if (agora == null) {
            return null;
        }
        Dia hoje = dados.dias().isEmpty() ? null : dados.dias().get(0);

        return new ClimaAtual(
                agora.temperatura(),
                CondicaoWmo.texto(agora.codigoWmo()),
                agora.categoria(),
                agora.ehDia(),
                agora.sensacaoTermica(),
                hoje == null ? null : hoje.maxima(),
                hoje == null ? null : hoje.minima(),
                agora.umidade(),
                new Vento(agora.ventoVelocidade(), direcao(agora.ventoDirecao())),
                new Medida(agora.rajada(), descricaoRajada(agora.rajada())),
                new Medida(agora.uv(), descricaoUv(agora.uv())),
                agora.chanceChuva(),
                hoje == null ? null : new Sol(hoje.nascerDoSol(), hoje.porDoSol()));
    }
    
 // As 48 horas a partir da hora atual, no fuso do local
    private List<HoraResponse> montarHoras(PrevisaoAtualizadaEvento dados) {
        LocalDateTime agora = LocalDateTime.now(ZoneId.of(dados.fuso())).truncatedTo(ChronoUnit.HOURS);

        return dados.horas().stream()
                .filter(h -> !h.horario().isBefore(agora))
                .limit(HORAS)
                .map(h -> new HoraResponse(h.horario(), h.temperatura(), h.chanceChuva(),
                        h.categoria(), h.ehDia()))
                .toList();
    }
    
    
    
    private List<DiaResponse> montarDias(PrevisaoAtualizadaEvento dados) {
        List<DiaResponse> resposta = new ArrayList<>();

        for (int i = 0; i < dados.dias().size(); i++) {
            Dia d = dados.dias().get(i);
            resposta.add(new DiaResponse(
                    d.data(),
                    d.minima(),
                    d.maxima(),
                    d.categoria(),
                    CondicaoWmo.texto(d.codigoWmo()),
                    d.chanceChuvaMaxima(),
                    d.chuvaMm(),
                    i >= DIAS_CONFIAVEIS));
        }
        return resposta;
    }
    
    
    // ---------- Classificacoes e conversoes ----------

    // 211 graus -> "SO"
    private String direcao(Integer graus) {
        if (graus == null) {
            return null;
        }
        return DIRECOES[(int) Math.round(graus / 45.0) % 8];
    }

    // 0-2 Baixo · 3-5 Moderado · 6-7 Alto · 8-10 Muito alto · 11+ Extremo
    private String descricaoUv(Double uv) {
        if (uv == null) {
            return null;
        }
        long valor = Math.round(uv);
        if (valor <= 2) return "Baixo";
        if (valor <= 5) return "Moderado";
        if (valor <= 7) return "Alto";
        if (valor <= 10) return "Muito alto";
        return "Extremo";
    }

    // abaixo de 30 km/h Fracas · 30-49 Moderadas · 50+ Fortes
    private String descricaoRajada(Double rajada) {
        if (rajada == null) {
            return null;
        }
        if (rajada < 30) return "Fracas";
        if (rajada < 50) return "Moderadas";
        return "Fortes";
    }

    // ---------- JSON ----------

    private String paraJson(PrevisaoAtualizadaEvento evento) {
        try {
            return objectMapper.writeValueAsString(evento);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Falha ao converter a previsao do endereco " + evento.enderecoId(), e);
        }
    }

    private PrevisaoAtualizadaEvento deJson(String json) {
        try {
            return objectMapper.readValue(json, PrevisaoAtualizadaEvento.class);
        } catch (Exception e) {
            throw new IllegalStateException("Falha ao ler a previsao guardada", e);
        }
    }
}