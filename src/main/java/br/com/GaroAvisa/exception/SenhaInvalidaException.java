package br.com.GaroAvisa.exception;

public class SenhaInvalidaException extends RuntimeException {

    public SenhaInvalidaException(String mensagem) {
        super(mensagem);
    }
}