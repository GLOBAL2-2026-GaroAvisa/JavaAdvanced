package br.com.GaroAvisa.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import br.com.GaroAvisa.evento.PrevisaoAtualizadaEvento;
import br.com.GaroAvisa.evento.PrevisaoAtualizadaEvento.Dia;
import br.com.GaroAvisa.model.AlertaClima;
import br.com.GaroAvisa.model.CategoriaClima;
import br.com.GaroAvisa.model.Endereco;
import br.com.GaroAvisa.model.MotivoMudanca;
import br.com.GaroAvisa.model.MudancaPrevisao;
import br.com.GaroAvisa.repository.EnderecoRepository;
import br.com.GaroAvisa.repository.MudancaPrevisaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Feature 21: compara a previsao que chegou com a que estava guardada.
// Roda dentro do consumidor do E2, ANTES de a previsao guardada ser substituida:
// e o unico momento em que as duas versoes existem ao mesmo tempo.
@Slf4j
@Service
@RequiredArgsConstructor
public class DetectorMudancaService {

    private static final int DIAS = 7;
    private static final double VARIACAO_TEMPERATURA = 4.0;
    private static final int CHUVA_BAIXA = 30;
    private static final int CHUVA_ALTA = 60;

    private final EnderecoRepository enderecoRepository;
    private final MudancaPrevisaoRepository mudancaRepository;

    public void detectar(PrevisaoAtualizadaEvento anterior, PrevisaoAtualizadaEvento nova,
                         Map<LocalDate, AlertaClima> alertasNovos) {

        Map<LocalDate, Dia> diasAnteriores = porData(anterior);
        int limite = Math.min(DIAS, nova.dias().size());
        Endereco endereco = null;

        for (int i = 0; i < limite; i++) {
            Dia agora = nova.dias().get(i);
            Dia antes = diasAnteriores.get(agora.data());
            if (antes == null) {
                continue;
            }

            AlertaClima alertaNovo = alertasNovos.get(agora.data());
            MotivoMudanca motivo = motivoDa(antes, agora, alertaNovo);
            if (motivo == null) {
                continue;
            }

            // Referencia sem SELECT, criada so se houver alguma mudanca para gravar
            if (endereco == null) {
                endereco = enderecoRepository.getReferenceById(nova.enderecoId());
            }
            registrar(endereco, agora.data(), motivo, antes, agora, alertaNovo, nova.atualizadoEm());
        }
    }

    // ---------- Regras ----------

    // A ORDEM DESTAS VERIFICACOES E A PRIORIDADE, igual a ordem do enum:
    // ALERTA > CEU > CHUVA > TEMPERATURA. O card e um so, entao um motivo ganha.
    // Devolve null quando nada mudou o suficiente.
    private MotivoMudanca motivoDa(Dia antes, Dia agora, AlertaClima alertaNovo) {
        if (alertaNovo != null) {
            return MotivoMudanca.ALERTA;
        }
        if (mudouDeGrupo(antes.categoria(), agora.categoria())) {
            return MotivoMudanca.CEU;
        }
        if (chuvaDisparou(antes.chanceChuvaMaxima(), agora.chanceChuvaMaxima())) {
            return MotivoMudanca.CHUVA;
        }
        if (variouTemperatura(antes.maxima(), agora.maxima())
                || variouTemperatura(antes.minima(), agora.minima())) {
            return MotivoMudanca.TEMPERATURA;
        }
        return null;
    }

    // A maxima ou a minima mudou 4 graus ou mais
    private boolean variouTemperatura(Double antes, Double agora) {
        return antes != null && agora != null && Math.abs(agora - antes) >= VARIACAO_TEMPERATURA;
    }

    // A chance de chuva passou de menos de 30% para 60% ou mais
    private boolean chuvaDisparou(Integer antes, Integer agora) {
        return antes != null && agora != null && antes < CHUVA_BAIXA && agora >= CHUVA_ALTA;
    }

    // O ceu mudou de grupo (ex.: sol -> chuva)
    private boolean mudouDeGrupo(CategoriaClima antes, CategoriaClima agora) {
        if (antes == null || agora == null
                || antes == CategoriaClima.DESCONHECIDA || agora == CategoriaClima.DESCONHECIDA) {
            return false;
        }
        return grupo(antes) != grupo(agora);
    }

    private enum GrupoCeu { SECO, CHUVOSO, INDEFINIDO }

    // Dois grupos apenas, aprovados pelo planejamento:
    // SECO = WMO 0-3, 45, 48        CHUVOSO = WMO 51-67, 71-77, 80-86, 95-99
    // Trocas dentro do mesmo grupo (sol -> nublado, chuva -> tempestade) nao
    // sao mudanca: com a tempestade separada, um unico ciclo gerou 26 cards de
    // ruido no teste. Tempestade sempre gera alerta ALTA, entao o gatilho
    // "surgiu um alerta novo" cobre esse caso com a gravidade certa.
    private GrupoCeu grupo(CategoriaClima categoria) {
        return switch (categoria) {
            case CEU_LIMPO, PARCIALMENTE_NUBLADO, NUBLADO, NEBLINA -> GrupoCeu.SECO;
            case GAROA, CHUVA, NEVE, TEMPESTADE -> GrupoCeu.CHUVOSO;
            case DESCONHECIDA -> GrupoCeu.INDEFINIDO;
        };
    }

    // ---------- Gravacao ----------

    private void registrar(Endereco endereco, LocalDate dia, MotivoMudanca motivo,
                           Dia antes, Dia agora, AlertaClima alertaNovo, Instant registradoEm) {

        Optional<MudancaPrevisao> existente =
                mudancaRepository.findByEnderecoIdAndDia(endereco.getId(), dia);

        if (motivo == MotivoMudanca.ALERTA) {
            existente.ifPresentOrElse(
                    m -> m.substituirPorAlerta(alertaNovo.getTipo(), alertaNovo.getGravidade(),
                            registradoEm),
                    () -> mudancaRepository.save(MudancaPrevisao.deAlerta(endereco, dia,
                            alertaNovo.getTipo(), alertaNovo.getGravidade(), registradoEm)));

            log.info("Mudanca brusca no endereco {} para o dia {} (ALERTA): novo alerta de {} {}",
                    endereco.getId(), dia, alertaNovo.getTipo(), alertaNovo.getGravidade());
            return;
        }

        String textoAntes = resumo(antes, motivo);
        String textoAgora = resumo(agora, motivo);
        CategoriaClima categoriaAntes = categoria(antes);
        CategoriaClima categoriaAgora = categoria(agora);

        existente.ifPresentOrElse(
                m -> m.substituirPorTexto(motivo, textoAntes, categoriaAntes,
                        textoAgora, categoriaAgora, registradoEm),
                () -> mudancaRepository.save(MudancaPrevisao.deTexto(endereco, dia, motivo,
                        textoAntes, categoriaAntes, textoAgora, categoriaAgora, registradoEm)));

        log.info("Mudanca brusca no endereco {} para o dia {} ({}): {} -> {}",
                endereco.getId(), dia, motivo, textoAntes, textoAgora);
    }

    // ---------- Auxiliares ----------

    private Map<LocalDate, Dia> porData(PrevisaoAtualizadaEvento previsao) {
        Map<LocalDate, Dia> mapa = new HashMap<>();
        for (Dia dia : previsao.dias()) {
            mapa.put(dia.data(), dia);
        }
        return mapa;
    }

    // CEU e TEMPERATURA: "27° e sol".  CHUVA: "25°, 70% de chuva",
    // porque nesse caso o que mudou foi a porcentagem, nao o ceu
    private String resumo(Dia dia, MotivoMudanca motivo) {
        String temperatura = dia.maxima() == null ? "--" : Math.round(dia.maxima()) + "°";

        if (motivo == MotivoMudanca.CHUVA) {
            String chance = dia.chanceChuvaMaxima() == null
                    ? "--"
                    : String.valueOf(dia.chanceChuvaMaxima());
            return temperatura + ", " + chance + "% de chuva";
        }
        return temperatura + " e " + categoria(dia).getResumo();
    }

    private CategoriaClima categoria(Dia dia) {
        return dia.categoria() == null ? CategoriaClima.DESCONHECIDA : dia.categoria();
    }
}