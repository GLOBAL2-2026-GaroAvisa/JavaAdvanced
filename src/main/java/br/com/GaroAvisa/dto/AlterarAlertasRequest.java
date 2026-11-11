package br.com.GaroAvisa.dto;

import jakarta.validation.constraints.NotNull;

public record AlterarAlertasRequest(

        @NotNull(message = "O campo receberAlertas é obrigatório")
        Boolean receberAlertas) {
}