package br.com.GaroAvisa.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import br.com.GaroAvisa.dto.AlertaResponse;
import br.com.GaroAvisa.evento.PrevisaoAtualizadaEvento.Dia;
import br.com.GaroAvisa.evento.PrevisaoAtualizadaEvento.Hora;

// Feature 23: o que e dito ao modelo. Fica separado do service porque e a
// parte que mais vai ser ajustada depois de ver os textos que saem.
public final class PromptExplicacao {

    private PromptExplicacao() {
    }

    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // As regras que o modelo tem que seguir. Tres delas vem direto do criterio
    // da feature: nao inventar dado, nao dar orientacao medica, texto curto
    public static final String SISTEMA = """
            Você escreve a "Explicação do Dia" de um aplicativo de previsão do tempo brasileiro.

            Regras:
            - Escreva de 2 a 3 frases curtas, em português do Brasil, com linguagem simples.
            - Use SOMENTE os números fornecidos. Nunca invente nenhum dado de clima.
            - Não dê orientação médica nem recomende remédios.
            - Não cumprimente, não se apresente e não faça perguntas.
            - Fale direto do dia, como quem avisa um amigo.
            - No máximo 400 caracteres.
            """;

    // Os dados do dia, em texto. Quando houver "Sobre mim", ele entra no fim:
    // e o que o criterio chama de personalizacao na Home
    public static String dados(String lugar, LocalDate dia, Dia hoje, List<Hora> horas,
                               List<AlertaResponse> alertas, String perfilIa) {
        StringBuilder texto = new StringBuilder();

        if (lugar != null) {
            texto.append("Lugar: ").append(lugar).append("\n");
        }
        texto.append("Data: ").append(dia.format(DATA)).append("\n");
        texto.append("Máxima: ").append(graus(hoje.maxima()))
                .append("   Mínima: ").append(graus(hoje.minima())).append("\n");
        texto.append("Maior chance de chuva do dia: ")
                .append(porcento(hoje.chanceChuvaMaxima())).append("\n");
        texto.append("Chuva por período: ").append(porPeriodo(horas)).append("\n");
        texto.append("Índice UV máximo: ").append(numero(hoje.uvMaximo())).append("\n");
        texto.append("Rajada máxima: ").append(kmh(hoje.rajadaMaxima())).append("\n");
        texto.append("Alertas: ").append(alertas(alertas)).append("\n");

        if (perfilIa != null && !perfilIa.isBlank()) {
            texto.append("\nSobre a pessoa: ").append(perfilIa.trim()).append("\n");
        }
        return texto.toString();
    }

    // "madrugada 10%, manha 20%, tarde 100%, noite 50%"
    private static String porPeriodo(List<Hora> horas) {
        if (horas == null || horas.isEmpty()) {
            return "sem dado";
        }
        return "madrugada " + maiorChance(horas, 0, 5)
                + ", manhã " + maiorChance(horas, 6, 11)
                + ", tarde " + maiorChance(horas, 12, 17)
                + ", noite " + maiorChance(horas, 18, 23);
    }

    private static String maiorChance(List<Hora> horas, int de, int ate) {
        Integer maior = null;
        for (Hora hora : horas) {
            int h = hora.horario().getHour();
            if (h >= de && h <= ate && hora.chanceChuva() != null
                    && (maior == null || hora.chanceChuva() > maior)) {
                maior = hora.chanceChuva();
            }
        }
        return porcento(maior);
    }

    private static String alertas(List<AlertaResponse> alertas) {
        if (alertas == null || alertas.isEmpty()) {
            return "nenhum";
        }
        StringBuilder lista = new StringBuilder();
        for (AlertaResponse alerta : alertas) {
            if (lista.length() > 0) {
                lista.append("; ");
            }
            lista.append(alerta.titulo()).append(" (").append(alerta.gravidade()).append(")");
        }
        return lista.toString();
    }

    private static String graus(Double valor) {
        return valor == null ? "sem dado" : Math.round(valor) + "°";
    }

    private static String porcento(Integer valor) {
        return valor == null ? "sem dado" : valor + "%";
    }

    private static String numero(Double valor) {
        return valor == null ? "sem dado" : String.valueOf(Math.round(valor));
    }

    private static String kmh(Double valor) {
        return valor == null ? "sem dado" : Math.round(valor) + " km/h";
    }
}