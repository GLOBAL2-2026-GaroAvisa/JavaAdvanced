package br.com.GaroAvisa.dto;

import java.time.Instant;
import java.time.LocalDate;

import br.com.GaroAvisa.model.CategoriaClima;
import br.com.GaroAvisa.model.GravidadeAlerta;
import br.com.GaroAvisa.model.Local;
import br.com.GaroAvisa.model.MotivoMudanca;
import br.com.GaroAvisa.model.MudancaPrevisao;
import br.com.GaroAvisa.model.TipoAlerta;

// Feature 21: um card de "O que mudou".
// A mudanca e guardada por endereco; o Local vem de quem monta a tela,
// porque a pessoa pensa em "Casa", nao em "endereco 10".
//
// O motivo diz quais campos vem preenchidos:
//   CEU, TEMPERATURA, CHUVA -> anterior e atualizada
//   ALERTA                  -> tipoAlerta e gravidadeAlerta
public record MudancaResponse(
        Long id,
        Long localId,
        String localApelido,
        LocalDate dia,
        Instant registradoEm,
        MotivoMudanca motivo,
        String anterior,
        CategoriaClima categoriaAnterior,
        String atualizada,
        CategoriaClima categoriaAtual,
        TipoAlerta tipoAlerta,
        GravidadeAlerta gravidadeAlerta) {

    public static MudancaResponse de(MudancaPrevisao m, Local local) {
        return new MudancaResponse(
                m.getId(),
                local.getId(),
                local.getApelido(),
                m.getDia(),
                m.getRegistradoEm(),
                m.getMotivo(),
                m.getAnterior(),
                m.getCategoriaAnterior(),
                m.getAtualizada(),
                m.getCategoriaAtual(),
                m.getTipoAlerta(),
                m.getGravidadeAlerta());
    }
}