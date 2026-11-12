package br.com.GaroAvisa.exception;

public class CredenciaisInvalidasException extends RuntimeException {

    public CredenciaisInvalidasException() {
        super("Credenciais inválidas");
    }
}