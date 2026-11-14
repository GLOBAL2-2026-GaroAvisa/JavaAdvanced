package br.com.GaroAvisa.model;

import java.util.Map;

// Sigla do estado a partir do nome por extenso (geocoding devolve o nome)
public final class Uf {

    private static final Map<String, String> SIGLAS = Map.ofEntries(
            Map.entry("acre", "AC"),
            Map.entry("alagoas", "AL"),
            Map.entry("amapa", "AP"),
            Map.entry("amazonas", "AM"),
            Map.entry("bahia", "BA"),
            Map.entry("ceara", "CE"),
            Map.entry("distrito federal", "DF"),
            Map.entry("espirito santo", "ES"),
            Map.entry("goias", "GO"),
            Map.entry("maranhao", "MA"),
            Map.entry("mato grosso", "MT"),
            Map.entry("mato grosso do sul", "MS"),
            Map.entry("minas gerais", "MG"),
            Map.entry("para", "PA"),
            Map.entry("paraiba", "PB"),
            Map.entry("parana", "PR"),
            Map.entry("pernambuco", "PE"),
            Map.entry("piaui", "PI"),
            Map.entry("rio de janeiro", "RJ"),
            Map.entry("rio grande do norte", "RN"),
            Map.entry("rio grande do sul", "RS"),
            Map.entry("rondonia", "RO"),
            Map.entry("roraima", "RR"),
            Map.entry("santa catarina", "SC"),
            Map.entry("sao paulo", "SP"),
            Map.entry("sergipe", "SE"),
            Map.entry("tocantins", "TO"));

    // Nao reconhecido (outro pais, grafia diferente) -> null, e o app mostra so a cidade
    public static String sigla(String estado) {
        if (estado == null || estado.isBlank()) {
            return null;
        }
        return SIGLAS.get(semAcento(estado.trim().toLowerCase()));
    }

    private static String semAcento(String texto) {
        return java.text.Normalizer.normalize(texto, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }

    private Uf() {
    }
}