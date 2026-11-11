package br.com.GaroAvisa.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.GaroAvisa.dto.AlterarAlertasRequest;
import br.com.GaroAvisa.dto.CriarLocalRequest;
import br.com.GaroAvisa.dto.LocalCardResponse;
import br.com.GaroAvisa.dto.PaginaResponse;
import br.com.GaroAvisa.exception.ApelidoJaCadastradoException;
import br.com.GaroAvisa.exception.FusoInvalidoException;
import br.com.GaroAvisa.exception.RecursoNaoEncontradoException;
import br.com.GaroAvisa.model.Endereco;
import br.com.GaroAvisa.model.Local;
import br.com.GaroAvisa.model.Usuario;
import br.com.GaroAvisa.repository.EnderecoRepository;
import br.com.GaroAvisa.repository.LocalRepository;
import br.com.GaroAvisa.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LocalService {

	private static final int TAMANHO_PADRAO = 10;
	private static final int TAMANHO_MAXIMO = 50;
	private static final int CASAS_COORDENADA = 6; // mesmo do NUMBER(9,6)

	private final LocalRepository localRepository;
	private final EnderecoRepository enderecoRepository;
	private final UsuarioRepository usuarioRepository;

	@Transactional(readOnly = true)
	public PaginaResponse<LocalCardResponse> listar(Long usuarioId, String busca, Integer pagina, Integer tamanho) {

		int paginaAjustada = (pagina == null || pagina < 0) ? 0 : pagina;
		int tamanhoAjustado = (tamanho == null || tamanho < 1) ? TAMANHO_PADRAO : Math.min(tamanho, TAMANHO_MAXIMO);
		String buscaAjustada = (busca == null || busca.isBlank()) ? null : busca.trim();

		Page<Local> locais = localRepository.buscarDoUsuario(usuarioId, buscaAjustada,
				PageRequest.of(paginaAjustada, tamanhoAjustado));

		return PaginaResponse.de(locais, LocalCardResponse::de);
	}

	// ---------- POST /locais ----------
	@Transactional
	public LocalCardResponse criar(Long usuarioId, CriarLocalRequest requisicao) {
		String apelido = requisicao.apelido().trim();

		// 1. Apelido único por usuário
		if (localRepository.existsByUsuarioIdAndApelido(usuarioId, apelido)) {
			throw new ApelidoJaCadastradoException();
		}

		// 2. Fuso precisa existir
		String fuso = validarFuso(requisicao.fuso());

		// 3. Caminho 2: reaproveita o endereço ou cria um novo
		Endereco endereco = buscarOuCriarEndereco(requisicao, fuso);

		// 4. Monta o local
		Usuario usuario = usuarioRepository.getReferenceById(usuarioId);
		Local local = new Local(usuario, endereco, apelido);

		// 5. Primeiro local do usuário vira principal
		local.setPrincipal(localRepository.countByUsuarioId(usuarioId) == 0);

		// 6. receberAlertas: se não vier, fica true (padrão da entidade)
		if (requisicao.receberAlertas() != null) {
			local.setReceberAlertas(requisicao.receberAlertas());
		}

		// 7. Grava
		try {
			localRepository.saveAndFlush(local);
		} catch (DataIntegrityViolationException ex) {
			// Dois pedidos simultâneos com o mesmo apelido: a UNIQUE do banco barra o
			// segundo
			throw new ApelidoJaCadastradoException();
		}

		return LocalCardResponse.de(local);
	}

	// ---------- DELETE /locais ----------
	@Transactional
	public void remover(Long usuarioId, Long localId) {

		Optional<Local> opLocal = localRepository.findByIdAndUsuarioId(usuarioId, localId);

		if (opLocal.isEmpty()) {
			throw new RecursoNaoEncontradoException("Local não encontrado");
		}

		Local localApagado = opLocal.get();

		localRepository.deleteById(localId);
		localRepository.flush();

		if (localApagado.getPrincipal()) {
			// local apagado era o principal

			List<Local> locais = localRepository.listarPorUsuario(usuarioId);

			if (!locais.isEmpty()) {
				Local novoLocalPrincipal = locais.getFirst();
				novoLocalPrincipal.setPrincipal(true);
				localRepository.save(novoLocalPrincipal);
			}

		}

	}
	
	// ---------- PATCH /locais/{id}/alertas ----------
	@Transactional
	public LocalCardResponse alterarAlertas(Long usuarioId, Long localId, AlterarAlertasRequest requisicao) {

		Local local = localRepository.findByIdAndUsuarioId(usuarioId, localId)
				.orElseThrow(() -> new RecursoNaoEncontradoException("Local não encontrado"));

		local.setReceberAlertas(requisicao.receberAlertas());

		return LocalCardResponse.de(local);
	}

	// ---------- Auxiliares ----------

	private String validarFuso(String fuso) {
		String fusoAjustado = fuso.trim();
		try {
			ZoneId.of(fusoAjustado);
		} catch (DateTimeException ex) {
			throw new FusoInvalidoException(fusoAjustado);
		}
		return fusoAjustado;
	}

	private Endereco buscarOuCriarEndereco(CriarLocalRequest requisicao, String fuso) {
		BigDecimal latitude = requisicao.latitude().setScale(CASAS_COORDENADA, RoundingMode.HALF_UP);
		BigDecimal longitude = requisicao.longitude().setScale(CASAS_COORDENADA, RoundingMode.HALF_UP);

		return enderecoRepository.findByLatitudeAndLongitude(latitude, longitude).orElseGet(
				() -> enderecoRepository.save(new Endereco(requisicao.pais().trim(), requisicao.estado().trim(),
						requisicao.cidade().trim(), requisicao.bairro().trim(), latitude, longitude, fuso)));
	}
}