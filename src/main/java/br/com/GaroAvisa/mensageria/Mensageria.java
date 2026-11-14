package br.com.GaroAvisa.mensageria;

public final class Mensageria {

    public static final String EXCHANGE = "garoavisa.eventos";

    // E1: pedidos de previsão (API -> Clima)
    public static final String RK_PREVISAO_SOLICITADA_LOCAL_CRIADO = "previsao.solicitada.local-criado";
    public static final String RK_PREVISAO_SOLICITADA_AGENDADA = "previsao.solicitada.agendada";

    // E2: previsões prontas (Clima -> API), usada no próximo evento
    public static final String RK_PREVISAO_ATUALIZADA = "previsao.atualizada";

    private Mensageria() {
    }
}