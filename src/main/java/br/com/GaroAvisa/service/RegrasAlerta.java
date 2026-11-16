package br.com.GaroAvisa.service;

import java.util.Set;

import br.com.GaroAvisa.model.GravidadeAlerta;
import br.com.GaroAvisa.model.TipoAlerta;

// Feature 80: TODOS os limites de alerta num lugar so.
// O planejamento avisou que estes valores sao iniciais e serao ajustados
// depois dos testes: mexer num alerta e mexer so neste arquivo.
// Classe final sem Spring de proposito: da para testar sem subir contexto.
public final class RegrasAlerta {

    private RegrasAlerta() {
    }

    // Chuva: maior chance de chuva do dia, em %
    public static final double CHUVA_MEDIA = 60;
    public static final double CHUVA_ALTA = 80;

    // Calor: maxima do dia, em C
    public static final double CALOR_MEDIA = 32;
    public static final double CALOR_ALTA = 35;

    // Frio: minima do dia, em C. Invertido: quanto menor, pior
    public static final double FRIO_MEDIA = 12;
    public static final double FRIO_ALTA = 5;

    // Rajada: maior rajada do dia, em km/h
    public static final double RAJADA_MEDIA = 50;
    public static final double RAJADA_ALTA = 70;

    // UV: maior indice do dia.
    // Limites subidos de 8/11 para 11/13 depois da medicao com dado real:
    // com 8, o alerta disparava em 13 dos 16 dias em Copacabana em novembro,
    // e um alerta que aparece todo dia deixa de ser lido.
    // 11 e o inicio da faixa "Extremo" da classificacao que o app ja exibe
    // no bloco de dados complementares (0-2 Baixo ... 8-10 Muito alto, 11+ Extremo).
    public static final double UV_MEDIA = 11;
    public static final double UV_ALTA = 13;

    // Codigos WMO de tempestade na Open-Meteo
    public static final Set<Integer> CODIGOS_TEMPESTADE = Set.of(95, 96, 99);

    public static GravidadeAlerta chuva(Integer chanceMaxima) {
        return crescente(paraDouble(chanceMaxima), CHUVA_MEDIA, CHUVA_ALTA);
    }

    public static GravidadeAlerta calor(Double maxima) {
        return crescente(maxima, CALOR_MEDIA, CALOR_ALTA);
    }

    public static GravidadeAlerta rajada(Double rajadaMaxima) {
        return crescente(rajadaMaxima, RAJADA_MEDIA, RAJADA_ALTA);
    }

    public static GravidadeAlerta uv(Double uvMaximo) {
        return crescente(uvMaximo, UV_MEDIA, UV_ALTA);
    }

    // Tempestade nao tem faixa: ou tem, e e sempre ALTA, ou nao tem
    public static GravidadeAlerta tempestade(Integer codigoWmo) {
        return codigoWmo != null && CODIGOS_TEMPESTADE.contains(codigoWmo)
                ? GravidadeAlerta.ALTA
                : null;
    }

    // Frio e o unico invertido: ALTA e o limite mais BAIXO
    public static GravidadeAlerta frio(Double minima) {
        if (minima == null) {
            return null;
        }
        if (minima <= FRIO_ALTA) {
            return GravidadeAlerta.ALTA;
        }
        if (minima <= FRIO_MEDIA) {
            return GravidadeAlerta.MEDIA;
        }
        return null;
    }

    // O limite que uma hora precisa cruzar para pertencer ao alerta daquela gravidade.
    // Usado para achar o horario: "Pancadas a partir das 15h"
    public static double limiar(TipoAlerta tipo, GravidadeAlerta gravidade) {
        boolean alta = gravidade == GravidadeAlerta.ALTA;
        return switch (tipo) {
            case CHUVA -> alta ? CHUVA_ALTA : CHUVA_MEDIA;
            case CALOR -> alta ? CALOR_ALTA : CALOR_MEDIA;
            case FRIO -> alta ? FRIO_ALTA : FRIO_MEDIA;
            case RAJADA -> alta ? RAJADA_ALTA : RAJADA_MEDIA;
            case UV -> alta ? UV_ALTA : UV_MEDIA;
            case TEMPESTADE -> 0; // nao usa limiar: compara o codigo WMO
        };
    }

    // MEDIA do limite de baixo ate ANTES do limite de cima: sem buracos.
    // 34,5 C cai em MEDIA; 35,0 cai em ALTA
    private static GravidadeAlerta crescente(Double valor, double media, double alta) {
        if (valor == null) {
            return null;
        }
        if (valor >= alta) {
            return GravidadeAlerta.ALTA;
        }
        if (valor >= media) {
            return GravidadeAlerta.MEDIA;
        }
        return null;
    }

    private static Double paraDouble(Integer valor) {
        return valor == null ? null : valor.doubleValue();
    }
}