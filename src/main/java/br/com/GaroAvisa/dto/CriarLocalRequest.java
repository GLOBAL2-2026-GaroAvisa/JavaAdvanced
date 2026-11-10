package br.com.GaroAvisa.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CriarLocalRequest(

        // ----- Dados do Local -----
        @NotBlank(message = "O apelido é obrigatório")
        @Size(max = 50, message = "O apelido deve ter no máximo 50 caracteres")
        String apelido,

        Boolean receberAlertas,

        // ----- Dados do Endereço (vêm da sugestão do geocoding) -----
        @NotBlank(message = "O bairro é obrigatório")
        @Size(max = 60, message = "O bairro deve ter no máximo 60 caracteres")
        String bairro,

        @NotBlank(message = "A cidade é obrigatória")
        @Size(max = 60, message = "A cidade deve ter no máximo 60 caracteres")
        String cidade,

        @NotBlank(message = "O estado é obrigatório")
        @Size(max = 50, message = "O estado deve ter no máximo 50 caracteres")
        String estado,

        @NotBlank(message = "O país é obrigatório")
        @Size(max = 50, message = "O país deve ter no máximo 50 caracteres")
        String pais,

        @NotNull(message = "A latitude é obrigatória")
        @DecimalMin(value = "-90", message = "Latitude inválida")
        @DecimalMax(value = "90", message = "Latitude inválida")
        BigDecimal latitude,

        @NotNull(message = "A longitude é obrigatória")
        @DecimalMin(value = "-180", message = "Longitude inválida")
        @DecimalMax(value = "180", message = "Longitude inválida")
        BigDecimal longitude,

        @NotBlank(message = "O fuso é obrigatório")
        @Size(max = 50, message = "O fuso deve ter no máximo 50 caracteres")
        String fuso) {
}