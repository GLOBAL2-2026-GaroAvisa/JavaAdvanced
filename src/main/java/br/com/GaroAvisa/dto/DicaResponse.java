package br.com.GaroAvisa.dto;

import br.com.GaroAvisa.model.TipoDica;

// Um chip de dica na Home. O tipo escolhe o icone; o texto ja vem pronto
public record DicaResponse(TipoDica tipo, String texto) {

    public static DicaResponse de(TipoDica tipo) {
        return new DicaResponse(tipo, tipo.getTexto());
    }
}