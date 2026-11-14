package br.com.GaroAvisa.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import br.com.GaroAvisa.model.CategoriaClima;

// Resposta do GET /locais/{id}/previsao: a tela Detalhes do local inteira
public record PrevisaoResponse(
        LocalResumo local,
        Instant atualizadoEm,
        ClimaAtual climaAtual,
        List<HoraResponse> previsaoPorHora,
        List<DiaResponse> previsaoDiaria,
        List<Object> alertas,
        List<Object> mudancasNaPrevisao) {

    public record LocalResumo(
            Long id,
            String apelido,
            String bairro,
            String cidade,
            String estado,
            String fuso) {
    }

    public record ClimaAtual(
            Double temperatura,
            String condicao,
            CategoriaClima categoria,
            Boolean ehDia,
            Double sensacaoTermica,
            Double temperaturaMaxima,
            Double temperaturaMinima,
            Integer umidade,
            Vento vento,
            Medida rajada,
            Medida indiceUV,
            Integer probabilidadeChuva,
            Sol sol) {
    }

    public record Vento(Double velocidade, String direcao) {
    }

    // Valor com a classificacao (UV: "Muito alto"; rajada: "Fortes")
    public record Medida(Double valor, String descricao) {
    }

    public record Sol(LocalDateTime nascer, LocalDateTime por) {
    }

    public record HoraResponse(
            LocalDateTime hora,
            Double temperatura,
            Integer probabilidadeChuva,
            CategoriaClima categoria,
            Boolean ehDia) {
    }

    public record DiaResponse(
            LocalDate data,
            Double temperaturaMinima,
            Double temperaturaMaxima,
            CategoriaClima categoria,
            String condicao,
            Integer probabilidadeChuva,
            Double volumeChuva,
            Boolean previsaoIncerta) {
    }
}