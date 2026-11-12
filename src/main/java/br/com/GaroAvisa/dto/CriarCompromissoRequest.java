package br.com.GaroAvisa.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CriarCompromissoRequest(

        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 80, message = "O nome deve ter no máximo 80 caracteres")
        String nome,

        @NotNull(message = "A data é obrigatória")
        LocalDate data) {
}