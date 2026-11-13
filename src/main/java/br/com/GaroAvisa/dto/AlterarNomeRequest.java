package br.com.GaroAvisa.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AlterarNomeRequest(

        @NotNull(message = "O nome é obrigatório")
        @Size(min = 3, max = 100, message = "O nome deve ter entre 3 e 100 caracteres")
        String nome) {

    public AlterarNomeRequest {
        nome = nome == null ? null : nome.trim();
    }
}