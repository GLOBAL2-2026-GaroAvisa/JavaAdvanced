package br.com.GaroAvisa.exception;

public class ApelidoJaCadastradoException extends RuntimeException {

    public ApelidoJaCadastradoException() {
        super("Já existe um local com este apelido");
    }
}