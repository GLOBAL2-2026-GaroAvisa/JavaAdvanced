package br.com.GaroAvisa.exception;

public class FusoInvalidoException extends RuntimeException {

    public FusoInvalidoException(String fuso) {
        super("Fuso horário inválido: " + fuso);
    }
}