package br.com.GaroAvisa.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @NotBlank(message = "O e-mail é obrigatório")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        String senha) {

    public LoginRequest {
        email = email == null ? null : email.trim().toLowerCase();
    }
}