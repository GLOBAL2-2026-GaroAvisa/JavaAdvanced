package br.com.GaroAvisa.dto;

import java.time.LocalDate;

import br.com.GaroAvisa.model.Compromisso;

public record CompromissoResponse(
        Long id,
        String nome,
        LocalDate data,
        Long localId,
        String localApelido) {

    public static CompromissoResponse de(Compromisso compromisso) {
        return new CompromissoResponse(
                compromisso.getId(),
                compromisso.getNome(),
                compromisso.getData(),
                compromisso.getLocal().getId(),
                compromisso.getLocal().getApelido());
    }
}