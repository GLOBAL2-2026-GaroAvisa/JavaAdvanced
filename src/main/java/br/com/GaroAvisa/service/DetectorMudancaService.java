package br.com.GaroAvisa.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import br.com.GaroAvisa.evento.PrevisaoAtualizadaEvento;
import br.com.GaroAvisa.evento.PrevisaoAtualizadaEvento.Dia;
import br.com.GaroAvisa.model.CategoriaClima;
import br.com.GaroAvisa.model.Local;
import br.com.GaroAvisa.model.MudancaPrevisao;
import br.com.GaroAvisa.repository.LocalRepository;
import br.com.GaroAvisa.repository.MudancaPrevisaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

// Feature 21: compara a previsao que chegou com a que estava guardada
@Slf4j
@Service
@RequiredArgsConstructor
public class DetectorMudancaService {

    private static final int DIAS = 7;
    private static final double VARIACAO_TEMPERATURA = 4.0;
    private static final int CHUVA_BAIXA = 30;
    private static final int CHUVA_ALTA = 60;

    private final LocalRepository localRepository;
    private final MudancaPrevisaoRepository mudancaRepository;

    public void detectar(PrevisaoAtualizadaEvento anterior, PrevisaoAtualizadaEvento nova) {

        List<Local> locais = localRepository.listarPorEndereco(nova.enderecoId());
        if (locais.isEmpty()) {
            return;
        }

        Map<LocalDate, Dia> diasAnteriores = porData(anterior);
        int limite = Math.min(DIAS, nova.dias().size());

        for (int i = 0; i < limite; i++) {
            Dia agora = nova.dias().get(i);
            Dia antes = diasAnteriores.get(agora.data());

            if (antes == null || !houveMudanca(antes, agora)) {
                continue;
            }

            for (Local local : locais) {
                registrar(local, agora.data(), antes, agora, nova.atualizadoEm());
            }

            log.info("Mudanca brusca no endereco {} para o dia {}: {} -> {}",
                    nova.enderecoId(), agora.data(), resumo(antes), resumo(agora));
        }
    }

    // ---------- Regras ----------

    private boolean houveMudanca(Dia antes, Dia agora) {
        return variouTemperatura(antes.maxima(), agora.maxima())
                || variouTemperatura(antes.minima(), agora.minima())
                || chuvaDisparou(antes.chanceChuvaMaxima(), agora.chanceChuvaMaxima())
                || mudouDeGrupo(antes.categoria(), agora.categoria());
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

    // A tempestade fica no grupo CHUVOSO: o codigo WMO diario oscila muito
    // entre chuva e tempestade, e isso geraria um card quase toda hora.
    // Tempestade sempre gera alerta de gravidade ALTA, entao o gatilho
    // "surgiu um alerta novo" (feature 18) cobre esse caso com o peso certo.
    private GrupoCeu grupo(CategoriaClima categoria) {
        return switch (categoria) {
            case CEU_LIMPO, PARCIALMENTE_NUBLADO, NUBLADO, NEBLINA -> GrupoCeu.SECO;
            case GAROA, CHUVA, NEVE, TEMPESTADE -> GrupoCeu.CHUVOSO;
            case DESCONHECIDA -> GrupoCeu.INDEFINIDO;
        };
    }

    // ---------- Gravacao ----------

    private void registrar(Local local, LocalDate dia, Dia antes, Dia agora, Instant registradoEm) {
        String textoAntes = resumo(antes);
        String textoAgora = resumo(agora);
        CategoriaClima categoriaAntes = categoria(antes);
        CategoriaClima categoriaAgora = categoria(agora);

        mudancaRepository.findByLocalIdAndDia(local.getId(), dia).ifPresentOrElse(
                m -> m.substituir(textoAntes, categoriaAntes, textoAgora, categoriaAgora, registradoEm),
                () -> mudancaRepository.save(new MudancaPrevisao(local, dia,
                        textoAntes, categoriaAntes, textoAgora, categoriaAgora, registradoEm)));
    }

    // ---------- Auxiliares ----------

    private Map<LocalDate, Dia> porData(PrevisaoAtualizadaEvento previsao) {
        Map<LocalDate, Dia> mapa = new HashMap<>();
        for (Dia dia : previsao.dias()) {
            mapa.put(dia.data(), dia);
        }
        return mapa;
    }

    // "27° e sol"
    private String resumo(Dia dia) {
        String temperatura = dia.maxima() == null ? "--" : Math.round(dia.maxima()) + "°";
        return temperatura + " e " + categoria(dia).getResumo();
    }

    private CategoriaClima categoria(Dia dia) {
        return dia.categoria() == null ? CategoriaClima.DESCONHECIDA : dia.categoria();
    }
}