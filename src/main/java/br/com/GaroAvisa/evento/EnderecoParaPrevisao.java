package br.com.GaroAvisa.evento;

import java.math.BigDecimal;

import br.com.GaroAvisa.model.Endereco;

public record EnderecoParaPrevisao(
        Long enderecoId,
        BigDecimal latitude,
        BigDecimal longitude,
        String fuso) {

    public static EnderecoParaPrevisao de(Endereco endereco) {
        return new EnderecoParaPrevisao(
                endereco.getId(),
                endereco.getLatitude(),
                endereco.getLongitude(),
                endereco.getFuso());
    }
}