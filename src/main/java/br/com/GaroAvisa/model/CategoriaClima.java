package br.com.GaroAvisa.model;

// As mesmas categorias que o Servico de Clima publica no evento PrevisaoAtualizada
public enum CategoriaClima {

    CEU_LIMPO("sol"),
    PARCIALMENTE_NUBLADO("sol entre nuvens"),
    NUBLADO("nublado"),
    NEBLINA("névoa"),
    GAROA("garoa"),
    CHUVA("chuva"),
    NEVE("neve"),
    TEMPESTADE("tempestade"),
    DESCONHECIDA("tempo indefinido");

    private final String resumo;

    CategoriaClima(String resumo) {
        this.resumo = resumo;
    }

    public String getResumo() {
        return resumo;
    }
}