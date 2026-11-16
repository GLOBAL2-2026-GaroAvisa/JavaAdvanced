package br.com.GaroAvisa.dto;

import java.time.LocalDate;

import br.com.GaroAvisa.model.AlertaClima;
import br.com.GaroAvisa.model.GravidadeAlerta;
import br.com.GaroAvisa.model.TipoAlerta;

// Um card de alerta de clima (features 18, 15 e 74).
// O titulo e a descricao sao montados AQUI, num lugar so: os mesmos textos
// vao servir aos avisos da aba Alertas quando o Epic 1 entrar.
// O tipo, a gravidade e o valor vao junto para o app escolher icone e cor.
public record AlertaResponse(
        Long id,
        TipoAlerta tipo,
        GravidadeAlerta gravidade,
        LocalDate dia,
        String titulo,
        String descricao,
        Double valor,
        Integer primeiraHora) {

    // Alerta guardado, de um local salvo
    public static AlertaResponse de(AlertaClima alerta) {
        return montar(alerta.getId(), alerta.getTipo(), alerta.getGravidade(),
                alerta.getDia(), alerta.getValor(), alerta.getPrimeiraHora());
    }

    // Alerta calculado na hora (Pesquisa): nao esta guardado, entao vem sem id
    public static AlertaResponse montar(Long id, TipoAlerta tipo, GravidadeAlerta gravidade,
                                        LocalDate dia, Double valor, Integer primeiraHora) {
        return new AlertaResponse(id, tipo, gravidade, dia,
                titulo(tipo, gravidade, primeiraHora),
                descricao(tipo, valor, primeiraHora),
                valor, primeiraHora);
    }

    // ---------- Textos ----------

    // "Chuva forte a tarde". Sem hora conhecida, fica so o nome
    private static String titulo(TipoAlerta tipo, GravidadeAlerta gravidade, Integer hora) {
        String nome = nome(tipo, gravidade);
        String periodo = periodo(hora);
        return periodo == null ? nome : nome + " " + periodo;
    }

    private static String nome(TipoAlerta tipo, GravidadeAlerta gravidade) {
        boolean alta = gravidade == GravidadeAlerta.ALTA;
        return switch (tipo) {
            case CHUVA -> alta ? "Chuva forte" : "Chuva";
            case TEMPESTADE -> "Tempestade";
            case CALOR -> alta ? "Calor forte" : "Calor";
            case FRIO -> alta ? "Frio intenso" : "Frio";
            case RAJADA -> alta ? "Rajadas fortes" : "Rajadas de vento";
            // Com o limite em 11, as duas faixas ja estao dentro do "Extremo"
            // da classificacao exibida na tela: a cor distingue a gravidade
            case UV -> "Índice UV extremo";
        };
    }

    // 0-5 madrugada · 6-11 manha · 12-17 tarde · 18-23 noite
    private static String periodo(Integer hora) {
        if (hora == null) {
            return null;
        }
        if (hora < 6) {
            return "de madrugada";
        }
        if (hora < 12) {
            return "de manhã";
        }
        if (hora < 18) {
            return "à tarde";
        }
        return "à noite";
    }

    // "Chance de chuva de 92%, a partir das 15h."
    private static String descricao(TipoAlerta tipo, Double valor, Integer hora) {
        String corpo = switch (tipo) {
            case CHUVA -> "Chance de chuva de " + inteiro(valor) + "%";
            case TEMPESTADE -> "Raios e rajadas previstos";
            case CALOR -> "Máxima de " + inteiro(valor) + "°";
            case FRIO -> "Mínima de " + inteiro(valor) + "°";
            case RAJADA -> "Rajadas de até " + inteiro(valor) + " km/h";
            case UV -> "Índice UV de " + inteiro(valor);
        };
        return hora == null ? corpo + "." : corpo + ", a partir das " + hora + "h.";
    }

    private static String inteiro(Double valor) {
        return valor == null ? "--" : String.valueOf(Math.round(valor));
    }
}	