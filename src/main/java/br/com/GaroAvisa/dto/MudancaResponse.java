package br.com.GaroAvisa.dto;

import java.time.Instant;
import java.time.LocalDate;

import br.com.GaroAvisa.model.CategoriaClima;
import br.com.GaroAvisa.model.MudancaPrevisao;

// Feature 21: um card de "O que mudou"
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

    public static MudancaResponse de(MudancaPrevisao m) {
        return new MudancaResponse(
                m.getId(),
                m.getLocal().getId(),
                m.getLocal().getApelido(),
                m.getDia(),
                m.getRegistradoEm(),
                m.getAnterior(),
                m.getCategoriaAnterior(),
                m.getAtualizada(),
                m.getCategoriaAtual());
    }
}