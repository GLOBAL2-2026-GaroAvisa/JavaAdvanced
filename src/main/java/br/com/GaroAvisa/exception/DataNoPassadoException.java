package br.com.GaroAvisa.exception;

public class DataNoPassadoException extends RuntimeException {

    public DataNoPassadoException() {
        super("A data do compromisso não pode estar no passado");
    }
}