package br.com.GaroAvisa.exception;

public class CoordenadaInvalidaException extends RuntimeException {

    public CoordenadaInvalidaException() {
        super("Latitude deve estar entre -90 e 90, e longitude entre -180 e 180.");
    }
}