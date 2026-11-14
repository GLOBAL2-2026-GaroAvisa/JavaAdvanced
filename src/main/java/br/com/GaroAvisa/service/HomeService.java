package br.com.GaroAvisa.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.GaroAvisa.dto.CompromissoResponse;
import br.com.GaroAvisa.dto.HomeResponse;
import br.com.GaroAvisa.dto.PrevisaoResponse;
import br.com.GaroAvisa.model.Local;
import br.com.GaroAvisa.repository.LocalRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HomeService {

    private static final int HORAS_HOME = 24;
    private static final int COMPROMISSOS_HOME = 2;

    private final LocalRepository localRepository;
    private final PrevisaoService previsaoService;
    private final CompromissoService compromissoService;

    @Transactional(readOnly = true)
    public HomeResponse buscar(Long usuarioId) {

        boolean temLocais = localRepository.countByUsuarioId(usuarioId) > 0;

        // Nao dependem do local principal
        List<CompromissoResponse> compromissos =
                compromissoService.listarProximos(usuarioId, 0, COMPROMISSOS_HOME).itens();

        Local principal = localRepository.findByUsuarioIdAndPrincipal(usuarioId, true).orElse(null);

        // Sem local principal: o app mostra o convite, de acordo com temLocais
        if (principal == null) {
            return new HomeResponse(temLocais, null, null, null, List.of(), null,
                    List.of(), List.of(), List.of(), compromissos);
        }

        PrevisaoResponse previsao = previsaoService.buscarDoLocal(usuarioId, principal.getId());

        return new HomeResponse(
                temLocais,
                previsao.local(),
                previsao.atualizadoEm(),
                previsao.climaAtual(),
                previsao.previsaoPorHora().stream().limit(HORAS_HOME).toList(),
                null,       // explicacaoDoDia: feature 23
                List.of(),  // alertas: feature 18
                List.of(),  // dicas: feature 20
                List.of(),  // mudancas na previsao: feature 21
                compromissos);
    }
}