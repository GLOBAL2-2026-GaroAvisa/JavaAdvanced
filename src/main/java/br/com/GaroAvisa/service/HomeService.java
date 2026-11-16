package br.com.GaroAvisa.service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.GaroAvisa.dto.AlertaResponse;
import br.com.GaroAvisa.dto.CompromissoResponse;
import br.com.GaroAvisa.dto.HomeResponse;
import br.com.GaroAvisa.dto.MudancaResponse;
import br.com.GaroAvisa.dto.PrevisaoResponse;
import br.com.GaroAvisa.model.Local;
import br.com.GaroAvisa.repository.LocalRepository;
import br.com.GaroAvisa.repository.MudancaPrevisaoRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HomeService {

    private static final int HORAS_HOME = 24;
    private static final int COMPROMISSOS_HOME = 2;
    private static final ZoneId FUSO_PADRAO = ZoneId.of("America/Sao_Paulo");

    private final LocalRepository localRepository;
    private final PrevisaoService previsaoService;
    private final CompromissoService compromissoService;
    private final MudancaPrevisaoRepository mudancaRepository;

    @Transactional(readOnly = true)
    public HomeResponse buscar(Long usuarioId) {

        List<Local> locais = localRepository.listarPorUsuario(usuarioId);
        boolean temLocais = !locais.isEmpty();

        // Nao dependem do local principal
        List<CompromissoResponse> compromissos =
                compromissoService.listarProximos(usuarioId, 0, COMPROMISSOS_HOME).itens();

        List<MudancaResponse> mudancas = montarMudancas(locais);

        Local principal = localRepository.findByUsuarioIdAndPrincipal(usuarioId, true).orElse(null);

        // Sem local principal: o app mostra o convite, de acordo com temLocais
        if (principal == null) {
            return new HomeResponse(
                    temLocais,
                    null,       // local
                    null,       // atualizadoEm
                    null,       // climaAtual
                    List.of(),  // previsaoPorHora
                    null,       // explicacaoDoDia: feature 23
                    List.of(),  // alertas
                    List.of(),  // dicas: feature 20
                    mudancas,   // feature 21
                    compromissos);
        }

        PrevisaoResponse previsao = previsaoService.buscarDoLocal(usuarioId, principal.getId());

        return new HomeResponse(
                temLocais,
                previsao.local(),
                previsao.atualizadoEm(),
                previsao.climaAtual(),
                previsao.previsaoPorHora().stream().limit(HORAS_HOME).toList(),
                null,                       // explicacaoDoDia: feature 23
                alertasDeHoje(previsao),    // feature 18
                List.of(),                  // dicas: feature 20
                mudancas,                   // feature 21
                compromissos);
    }

    // Feature 18: a Home mostra so os alertas de HOJE do local principal.
    // Os Detalhes ja trouxeram hoje e os proximos dias, entao aqui e so filtrar:
    // uma ida a menos ao banco
    private List<AlertaResponse> alertasDeHoje(PrevisaoResponse previsao) {
        if (previsao.local() == null || previsao.local().fuso() == null) {
            return List.of();
        }
        LocalDate hoje = LocalDate.now(ZoneId.of(previsao.local().fuso()));
        return previsao.alertas().stream()
                .filter(alerta -> alerta.dia().equals(hoje))
                .toList();
    }

    // As mudancas sao guardadas por endereco. A Home traz as de todos os locais
    // do usuario, e cada card mostra o apelido do Local, nao o endereco.
    private List<MudancaResponse> montarMudancas(List<Local> locais) {
        if (locais.isEmpty()) {
            return List.of();
        }

        // Um mesmo endereco pode ser usado por mais de um local do usuario
        // (apelidos diferentes na mesma coordenada): nesse caso a mudanca
        // aparece uma vez por local, que e o que a pessoa espera ver.
        Map<Long, List<Local>> locaisPorEndereco = locais.stream()
                .collect(Collectors.groupingBy(l -> l.getEndereco().getId()));

        return mudancaRepository
                .buscarDosEnderecos(locaisPorEndereco.keySet(), LocalDate.now(FUSO_PADRAO))
                .stream()
                .flatMap(m -> locaisPorEndereco.get(m.getEndereco().getId()).stream()
                        .map(local -> MudancaResponse.de(m, local)))
                .toList();
    }
}