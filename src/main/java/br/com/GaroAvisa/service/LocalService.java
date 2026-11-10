package br.com.GaroAvisa.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.GaroAvisa.dto.LocalCardResponse;
import br.com.GaroAvisa.dto.PaginaResponse;
import br.com.GaroAvisa.model.Local;
import br.com.GaroAvisa.repository.LocalRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LocalService {

    private static final int TAMANHO_PADRAO = 10;
    private static final int TAMANHO_MAXIMO = 50;

    private final LocalRepository localRepository;

    @Transactional(readOnly = true)
    public PaginaResponse<LocalCardResponse> listar(Long usuarioId, String busca,
                                                     Integer pagina, Integer tamanho) {

        int paginaAjustada = (pagina == null || pagina < 0) ? 0 : pagina;
        int tamanhoAjustado = (tamanho == null || tamanho < 1) ? TAMANHO_PADRAO
                : Math.min(tamanho, TAMANHO_MAXIMO);
        String buscaAjustada = (busca == null || busca.isBlank()) ? null : busca.trim();

        Page<Local> locais = localRepository.buscarDoUsuario(
                usuarioId, buscaAjustada, PageRequest.of(paginaAjustada, tamanhoAjustado));

        return PaginaResponse.de(locais, LocalCardResponse::de);
    }
}