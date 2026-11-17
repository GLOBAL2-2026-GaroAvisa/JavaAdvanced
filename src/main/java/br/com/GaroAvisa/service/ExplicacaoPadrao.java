package br.com.GaroAvisa.service;

import java.util.List;
import java.util.stream.Collectors;

import br.com.GaroAvisa.dto.AlertaResponse;
import br.com.GaroAvisa.evento.PrevisaoAtualizadaEvento.Dia;

// Feature 23: o texto montado pelas REGRAS, sem IA.
// Usado quando a IA falha, demora ou nao esta configurada - e e o que aparece
// enquanto a chave nao existe. Nunca e guardado no cache.
public final class ExplicacaoPadrao {

    private ExplicacaoPadrao() {
    }

    // So menciona chuva quando ela e relevante o bastante para mudar o dia
    private static final int CHUVA_MENCIONA = 40;

    public static String de(Dia hoje, List<AlertaResponse> alertas) {
        if (hoje == null) {
            return "Ainda não temos a previsão de hoje para este lugar.";
        }

        StringBuilder texto = new StringBuilder();
        texto.append("Hoje a máxima é de ").append(graus(hoje.maxima()))
                .append(" e a mínima de ").append(graus(hoje.minima())).append(".");

        if (hoje.chanceChuvaMaxima() != null && hoje.chanceChuvaMaxima() >= CHUVA_MENCIONA) {
            texto.append(" A chance de chuva chega a ")
                    .append(hoje.chanceChuvaMaxima()).append("%.");
        }

        if (alertas != null && !alertas.isEmpty()) {
            texto.append(" Fique atento: ").append(listar(alertas)).append(".");
        }
        return texto.toString();
    }

    // "chuva forte à tarde e tempestade à tarde"
    private static String listar(List<AlertaResponse> alertas) {
        List<String> titulos = alertas.stream()
                .map(alerta -> minuscula(alerta.titulo()))
                .toList();

        if (titulos.size() == 1) {
            return titulos.get(0);
        }
        String inicio = titulos.subList(0, titulos.size() - 1).stream()
                .collect(Collectors.joining(", "));
        return inicio + " e " + titulos.get(titulos.size() - 1);
    }

    private static String minuscula(String texto) {
        return texto.isEmpty() ? texto
                : Character.toLowerCase(texto.charAt(0)) + texto.substring(1);
    }

    private static String graus(Double valor) {
        return valor == null ? "--" : Math.round(valor) + "°";
    }
}