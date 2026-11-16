package br.com.GaroAvisa.dto;

import java.time.Instant;
import java.util.List;

// Resposta do GET /home: a tela inicial inteira
public record HomeResponse(
        boolean temLocais,
        PrevisaoResponse.LocalResumo local,
        Instant atualizadoEm,
        PrevisaoResponse.ClimaAtual climaAtual,
        List<PrevisaoResponse.HoraResponse> previsaoPorHora,
        String explicacaoDoDia,
        List<AlertaResponse> alertas,
        List<Object> dicas,
        List<MudancaResponse> mudancasNaPrevisao,
        List<CompromissoResponse> compromissos) {
}