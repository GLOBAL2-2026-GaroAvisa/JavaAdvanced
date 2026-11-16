package br.com.GaroAvisa.dto;

import java.time.Instant;
import java.time.LocalDate;

import br.com.GaroAvisa.model.CategoriaClima;
import br.com.GaroAvisa.model.Local;
import br.com.GaroAvisa.model.MudancaPrevisao;

// Feature 21: um card de "O que mudou".
// A mudanca e guardada por endereco; o Local vem de quem monta a tela,
// porque a pessoa pensa em "Casa", nao em "endereco 10".
public record MudancaResponse(
        Long id,
        Long localId,
        String localApelido,
        LocalDate dia,
        Instant registradoEm,
        String anterior,
        CategoriaClima categoriaAnterior,
        String atualizada,
        CategoriaClima categoriaAtual) {

    public static MudancaResponse de(MudancaPrevisao m, Local local) {
        return new MudancaResponse(
                m.getId(),
                local.getId(),
                local.getApelido(),
                m.getDia(),
                m.getRegistradoEm(),
                m.getAnterior(),
                m.getCategoriaAnterior(),
                m.getAtualizada(),
                m.getCategoriaAtual());
    }
}