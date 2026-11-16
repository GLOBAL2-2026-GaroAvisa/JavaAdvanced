package br.com.GaroAvisa.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import br.com.GaroAvisa.evento.PrevisaoAtualizadaEvento;
import br.com.GaroAvisa.evento.PrevisaoAtualizadaEvento.Dia;
import br.com.GaroAvisa.model.CategoriaClima;
import br.com.GaroAvisa.model.Endereco;
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

    public void detectar(PrevisaoAtualizadaEvento anterior, PrevisaoAtualizadaEvento nova) {

        Map<LocalDate, Dia> diasAnteriores = porData(anterior);
        int limite = Math.min(DIAS, nova.dias().size());
        Endereco endereco = null;

        for (int i = 0; i < limite; i++) {
            Dia agora = nova.dias().get(i);
            Dia antes = diasAnteriores.get(agora.data());

            if (antes == null || !houveMudanca(antes, agora)) {
                continue;
            }

            // Referencia sem SELECT, criada so se houver alguma mudanca para gravar
            if (endereco == null) {
                endereco = enderecoRepository.getReferenceById(nova.enderecoId());
            }
            registrar(endereco, agora.data(), antes, agora, nova.atualizadoEm());

            log.info("Mudanca brusca no endereco {} para o dia {}: {} -> {}",
                    nova.enderecoId(), agora.data(), resumo(antes), resumo(agora));
        }
    }

    // ---------- Regras ----------

    // Falta o quarto gatilho, "surge um alerta novo", que depende da feature 80.
    // Quando entrar, ele vale so de AMANHA em diante: um alerta novo para hoje
    // ja gera um aviso de origem DIA, e contar os dois seria o mesmo fato duas vezes.
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

    // Dois grupos apenas, aprovados pelo planejamento:
    // SECO  = WMO 0-3, 45, 48        CHUVOSO = WMO 51-67, 71-77, 80-86, 95-99
    // A tempestade fica no grupo CHUVOSO porque o codigo WMO diario oscila muito
    // entre chuva e tempestade: com ela separada, um unico ciclo gerou 26 cards
    // de ruido no teste. Tempestade sempre gera alerta ALTA, entao o gatilho
    // "surgiu um alerta novo" cobre esse caso com a gravidade certa.
    private GrupoCeu grupo(CategoriaClima categoria) {
        return switch (categoria) {
            case CEU_LIMPO, PARCIALMENTE_NUBLADO, NUBLADO, NEBLINA -> GrupoCeu.SECO;
            case GAROA, CHUVA, NEVE, TEMPESTADE -> GrupoCeu.CHUVOSO;
            case DESCONHECIDA -> GrupoCeu.INDEFINIDO;
        };
    }

    // ---------- Gravacao ----------

    private void registrar(Endereco endereco, LocalDate dia, Dia antes, Dia agora, Instant registradoEm) {
        String textoAntes = resumo(antes);
        String textoAgora = resumo(agora);
        CategoriaClima categoriaAntes = categoria(antes);
        CategoriaClima categoriaAgora = categoria(agora);

        mudancaRepository.findByEnderecoIdAndDia(endereco.getId(), dia).ifPresentOrElse(
                m -> m.substituir(textoAntes, categoriaAntes, textoAgora, categoriaAgora, registradoEm),
                () -> mudancaRepository.save(new MudancaPrevisao(endereco, dia,
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