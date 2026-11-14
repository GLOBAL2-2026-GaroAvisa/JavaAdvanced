package br.com.GaroAvisa.model;

import java.util.Map;

// Texto da condicao do tempo a partir do codigo WMO da Open-Meteo
public final class CondicaoWmo {

    private static final String INDISPONIVEL = "Indisponível";

    private static final Map<Integer, String> TEXTOS = Map.ofEntries(
            Map.entry(0, "Céu limpo"),
            Map.entry(1, "Predominantemente limpo"),
            Map.entry(2, "Parcialmente nublado"),
            Map.entry(3, "Nublado"),
            Map.entry(45, "Névoa"),
            Map.entry(48, "Névoa com geada"),
            Map.entry(51, "Garoa fraca"),
            Map.entry(53, "Garoa moderada"),
            Map.entry(55, "Garoa forte"),
            Map.entry(56, "Garoa congelante fraca"),
            Map.entry(57, "Garoa congelante forte"),
            Map.entry(61, "Chuva fraca"),
            Map.entry(63, "Chuva moderada"),
            Map.entry(65, "Chuva forte"),
            Map.entry(66, "Chuva congelante fraca"),
            Map.entry(67, "Chuva congelante forte"),
            Map.entry(71, "Neve fraca"),
            Map.entry(73, "Neve moderada"),
            Map.entry(75, "Neve forte"),
            Map.entry(77, "Grãos de neve"),
            Map.entry(80, "Pancadas de chuva fracas"),
            Map.entry(81, "Pancadas de chuva moderadas"),
            Map.entry(82, "Pancadas de chuva fortes"),
            Map.entry(85, "Pancadas de neve fracas"),
            Map.entry(86, "Pancadas de neve fortes"),
            Map.entry(95, "Tempestade"),
            Map.entry(96, "Tempestade com granizo leve"),
            Map.entry(99, "Tempestade com granizo forte"));

    public static String texto(Integer codigo) {
        return codigo == null ? INDISPONIVEL : TEXTOS.getOrDefault(codigo, INDISPONIVEL);
    }

    private CondicaoWmo() {
    }
}