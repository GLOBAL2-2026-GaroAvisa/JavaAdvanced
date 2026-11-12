package br.com.GaroAvisa.service;

import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.GaroAvisa.dto.CompromissoResponse;
import br.com.GaroAvisa.dto.CriarCompromissoRequest;
import br.com.GaroAvisa.dto.PaginaResponse;
import br.com.GaroAvisa.exception.DataNoPassadoException;
import br.com.GaroAvisa.exception.RecursoNaoEncontradoException;
import br.com.GaroAvisa.model.Compromisso;
import br.com.GaroAvisa.model.Local;
import br.com.GaroAvisa.repository.CompromissoRepository;
import br.com.GaroAvisa.repository.LocalRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompromissoService {

    private static final int TAMANHO_PADRAO = 10;
    private static final int TAMANHO_MAXIMO = 50;
    // Usado só na listagem geral, que mistura locais (ver observação)
    private static final ZoneId FUSO_PADRAO = ZoneId.of("America/Sao_Paulo");

    private final CompromissoRepository compromissoRepository;
    private final LocalRepository localRepository;

    // ---------- POST /locais/{localId}/compromissos ----------
    @Transactional
    public CompromissoResponse criar(Long usuarioId, Long localId, CriarCompromissoRequest requisicao) {

        Local local = buscarLocalDoUsuario(usuarioId, localId);

        validarData(requisicao.data(), local);

        Compromisso compromisso = new Compromisso(local, requisicao.nome().trim(), requisicao.data());
        compromissoRepository.save(compromisso);

        return CompromissoResponse.de(compromisso);
    }

    // ---------- GET /compromissos ----------
    @Transactional(readOnly = true)
    public PaginaResponse<CompromissoResponse> listarProximos(Long usuarioId, Integer pagina, Integer tamanho) {

        LocalDate hoje = LocalDate.now(FUSO_PADRAO);

        Page<Compromisso> compromissos = compromissoRepository.buscarProximosDoUsuario(
                usuarioId, hoje, paginacao(pagina, tamanho));

        return PaginaResponse.de(compromissos, CompromissoResponse::de);
    }

    // ---------- GET /locais/{localId}/compromissos ----------
    @Transactional(readOnly = true)
    public PaginaResponse<CompromissoResponse> listarProximosDoLocal(Long usuarioId, Long localId,
                                                                      Integer pagina, Integer tamanho) {

        Local local = buscarLocalDoUsuario(usuarioId, localId);

        Page<Compromisso> compromissos = compromissoRepository.buscarProximosDoLocal(
                localId, hojeNoLocal(local), paginacao(pagina, tamanho));

        return PaginaResponse.de(compromissos, CompromissoResponse::de);
    }

    // ---------- Auxiliares ----------

    private Local buscarLocalDoUsuario(Long usuarioId, Long localId) {
        return localRepository.findByIdAndUsuarioId(usuarioId, localId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Local não encontrado"));
    }

    private LocalDate hojeNoLocal(Local local) {
        return LocalDate.now(ZoneId.of(local.getEndereco().getFuso()));
    }

    private void validarData(LocalDate data, Local local) {
        if (data.isBefore(hojeNoLocal(local))) {
            throw new DataNoPassadoException();
        }
    }

    private Pageable paginacao(Integer pagina, Integer tamanho) {
        int paginaAjustada = (pagina == null || pagina < 0) ? 0 : pagina;
        int tamanhoAjustado = (tamanho == null || tamanho < 1) ? TAMANHO_PADRAO
                : Math.min(tamanho, TAMANHO_MAXIMO);
        return PageRequest.of(paginaAjustada, tamanhoAjustado);
    }
}