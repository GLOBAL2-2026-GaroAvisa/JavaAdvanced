package br.com.GaroAvisa.model;

import lombok.Getter;

// Feature 20: as cinco dicas de hoje. O texto fica aqui porque e
// constante por tipo, igual ao resumo da CategoriaClima
@Getter
public enum TipoDica {

    GUARDA_CHUVA("Leve guarda-chuva"),
    PROTETOR_SOLAR("Use protetor solar"),
    OCULOS_ESCUROS("Leve óculos escuros"),
    CASACO("Leve um casaco"),
    GARRAFA_AGUA("Leve uma garrafa de água");

    private final String texto;

    TipoDica(String texto) {
        this.texto = texto;
    }
}