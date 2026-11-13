package br.com.GaroAvisa.dto;

import jakarta.validation.constraints.Size;

public record AlterarPerfilIaRequest(

        @Size(max = 200, message = "O \"Sobre mim\" deve ter no máximo 200 caracteres")
        String perfilIa) {

    public AlterarPerfilIaRequest {
        perfilIa = (perfilIa == null || perfilIa.isBlank()) ? null : perfilIa.trim();
    }
}