package br.com.GaroAvisa.service;

// Feature 20: todos os limites das dicas num lugar so, no molde da RegrasAlerta.
// Valores iniciais: o planejamento avisou que serao ajustados apos os testes
public final class RegrasDica {

    private RegrasDica() {
    }

    // Guarda-chuva: chance de chuva em alguma hora entre 7h e 22h
    public static final int GUARDA_CHUVA_CHANCE = 50;
    public static final int GUARDA_CHUVA_HORA_INICIO = 7;
    public static final int GUARDA_CHUVA_HORA_FIM = 22;

    // Protetor solar e oculos escuros: maior UV do dia
    public static final double UV_MINIMO = 6;

    // Oculos escuros tambem exige pouca chuva: maior chance do dia abaixo disso
    public static final int POUCA_CHUVA = 30;

    // Casaco: minima baixa OU variacao grande no dia
    public static final double CASACO_MINIMA = 16;
    public static final double CASACO_VARIACAO = 10;

    // Garrafa de agua: maxima do dia
    public static final double AGUA_MAXIMA = 28;
}