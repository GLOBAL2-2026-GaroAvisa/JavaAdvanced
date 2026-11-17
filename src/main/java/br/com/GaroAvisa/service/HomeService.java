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
import br.com.GaroAvisa.dto.DicaResponse;
import br.com.GaroAvisa.dto.ExplicacaoResponse;
import br.com.GaroAvisa.dto.HomeResponse;
import br.com.GaroAvisa.dto.MudancaResponse;
import br.com.GaroAvisa.dto.PrevisaoResponse;
import br.com.GaroAvisa.exception.RecursoNaoEncontradoException;
import br.com.GaroAvisa.model.Local;
import br.com.GaroAvisa.model.Usuario;
import br.com.GaroAvisa.repository.LocalRepository;
import br.com.GaroAvisa.repository.MudancaPrevisaoRepository;
import br.com.GaroAvisa.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class HomeService {

    private static final int HORAS_HOME = 24;
    private static final int COMPROMISSOS_HOME = 2;
    private static final ZoneId FUSO_PADRAO = ZoneId.of("America/Sao_Paulo");

    private final LocalRepository localRepository;
    private final UsuarioRepository usuarioRepository;
    private final PrevisaoService previsaoService;
    private final CompromissoService compromissoService;
    private final MudancaPrevisaoRepository mudancaRepository;
    private final DicaService dicaService;
    private final ExplicacaoDiaService explicacaoService;

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
                    null,       // explicacaoDoDia
                    false,      // explicacaoGeradaPorIa
                    List.of(),  // alertas
                    List.of(),  // dicas
                    mudancas,
                    compromissos);
        }

        PrevisaoService.PrevisaoDoLocal doLocal =
                previsaoService.buscarDoLocalComDados(usuarioId, principal.getId());
        PrevisaoResponse previsao = doLocal.resposta();

        // Feature 23: so o que JA esta no cache. A Home nunca espera a IA:
        // vindo nulo, o app pede no GET /home/explicacao
        String explicacao = explicacaoDoCache(usuarioId, principal, previsao);

        return new HomeResponse(
                temLocais,
                previsao.local(),
                previsao.atualizadoEm(),
                previsao.climaAtual(),
                previsao.previsaoPorHora().stream().limit(HORAS_HOME).toList(),
                explicacao,
                explicacao != null,       // so ha texto em cache se veio da IA
                alertasDeHoje(previsao),  // feature 18
                dicasDeHoje(doLocal),     // feature 20
                mudancas,                 // feature 21
                compromissos);
    }

    // ---------- Feature 23 ----------

    private String explicacaoDoCache(Long usuarioId, Local principal, PrevisaoResponse previsao) {
        if (previsao.local() == null || previsao.local().fuso() == null) {
            return null;
        }
        LocalDate hoje = LocalDate.now(ZoneId.of(previsao.local().fuso()));
        return explicacaoService.doCache(
                ExplicacaoDiaService.chaveDaHome(usuarioId, principal.getId(), hoje));
    }

    /**
     * GET /home/explicacao: gera o texto se ainda nao existir.
     * E a unica parte da Home que pode demorar, e por isso mora num endpoint
     * separado - assim a tela inicial carrega sempre no mesmo tempo.
     */
    @Transactional
    public ExplicacaoResponse explicacaoDoPrincipal(Long usuarioId) {
        Local principal = localRepository.findByUsuarioIdAndPrincipal(usuarioId, true)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Nenhum local principal"));

        PrevisaoService.PrevisaoDoLocal doLocal =
                previsaoService.buscarDoLocalComDados(usuarioId, principal.getId());

        // Local recem-criado: a previsao ainda esta sendo buscada
        if (doLocal.dados() == null) {
            return new ExplicacaoResponse("Ainda estamos buscando a previsão deste local.", false);
        }

        PrevisaoResponse previsao = doLocal.resposta();
        LocalDate hoje = LocalDate.now(ZoneId.of(previsao.local().fuso()));

        String perfilIa = usuarioRepository.findById(usuarioId)
                .map(Usuario::getPerfilIa)
                .orElse(null);

        String lugar = previsao.local().nome() + ", " + previsao.local().cidade();

        return explicacaoService.obter(
                ExplicacaoDiaService.chaveDaHome(usuarioId, principal.getId(), hoje),
                usuarioId,
                hoje,
                lugar,
                perfilIa,
                ExplicacaoDiaService.diaDe(doLocal.dados(), hoje),
                ExplicacaoDiaService.horasDe(doLocal.dados(), hoje),
                alertasDeHoje(previsao));
    }

    // ---------- Feature 18 ----------

    // A Home mostra so os alertas de HOJE do local principal. Os Detalhes ja
    // trouxeram hoje e os proximos dias, entao aqui e so filtrar
    private List<AlertaResponse> alertasDeHoje(PrevisaoResponse previsao) {
        if (previsao.local() == null || previsao.local().fuso() == null) {
            return List.of();
        }
        LocalDate hoje = LocalDate.now(ZoneId.of(previsao.local().fuso()));
        return previsao.alertas().stream()
                .filter(alerta -> alerta.dia().equals(hoje))
                .toList();
    }

    // ---------- Feature 20 ----------

    private List<DicaResponse> dicasDeHoje(PrevisaoService.PrevisaoDoLocal doLocal) {
        if (doLocal.dados() == null || doLocal.resposta().local() == null) {
            return List.of();
        }
        LocalDate hoje = LocalDate.now(ZoneId.of(doLocal.resposta().local().fuso()));
        return dicaService.calcular(doLocal.dados(), hoje);
    }

    // ---------- Feature 21 ----------

    // As mudancas sao guardadas por endereco. A Home traz as de todos os locais
    // do usuario, e cada card mostra o apelido do Local, nao o endereco.
    private List<MudancaResponse> montarMudancas(List<Local> locais) {
        if (locais.isEmpty()) {
            return List.of();
        }

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