package br.com.GaroAvisa.dto;

import java.time.Instant;
import java.util.List;

// Resposta do GET /previsao: a tela de Pesquisa (lugar nao salvo)
public record PesquisaResponse(
        Instant atualizadoEm,
        PrevisaoResponse.ClimaAtual climaAtual,
        List<PrevisaoResponse.HoraResponse> previsaoPorHora,
        List<PrevisaoResponse.DiaResponse> previsaoDiaria,
        String explicacaoDoDia,
        boolean explicacaoGeradaPorIa,
        List<AlertaResponse> alertas) {
}