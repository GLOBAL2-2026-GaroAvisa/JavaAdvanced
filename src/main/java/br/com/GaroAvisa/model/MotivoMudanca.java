package br.com.GaroAvisa.model;

// Feature 21: o que disparou a mudanca. A ORDEM DE DECLARACAO E A PRIORIDADE:
// quando mais de um gatilho dispara no mesmo dia, o card mostra o primeiro
// desta lista, porque o card e um so.
public enum MotivoMudanca {

    // Nasceu um alerta para aquele dia. Carrega gravidade e tipo, e e o unico
    // que avisa com antecedencia de algo que pode mudar o plano da pessoa
    ALERTA,

    // O ceu passou de seco para chuvoso, ou o contrario
    CEU,

    // A chance de chuva passou de menos de 30% para 60% ou mais, sem trocar de grupo
    CHUVA,

    // A maxima ou a minima mudou 4 graus ou mais
    TEMPERATURA
}