package br.com.GaroAvisa.mensageria;

import br.com.GaroAvisa.evento.EnderecoParaPrevisao;

// Evento INTERNO do Spring (não vai para o RabbitMQ)
public record EnderecoNovoCriado(EnderecoParaPrevisao endereco) {
}