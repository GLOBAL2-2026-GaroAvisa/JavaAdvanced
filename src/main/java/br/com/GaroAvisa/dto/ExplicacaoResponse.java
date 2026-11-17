package br.com.GaroAvisa.dto;

// Feature 23: o texto da Explicacao do Dia.
// O geradaPorIa diz se o texto veio do modelo ou das regras. O app mostra o
// selo "IA" de qualquer forma - o campo serve para saber, de fora, se a IA
// esta funcionando em producao
public record ExplicacaoResponse(String texto, boolean geradaPorIa) {
}