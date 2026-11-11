package br.com.GaroAvisa.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EditarLocalRequest(

        @NotBlank(message = "O apelido é obrigatório")
        @Size(max = 50, message = "O apelido deve ter no máximo 50 caracteres")
        String apelido,

        @NotNull(message = "O campo receberAlertas é obrigatório")
        Boolean receberAlertas,

        @NotNull(message = "O campo principal é obrigatório")
        Boolean principal) {
}