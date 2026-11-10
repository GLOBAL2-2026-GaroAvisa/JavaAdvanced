package br.com.GaroAvisa.dto;

import br.com.GaroAvisa.model.Local;

public record LocalCardResponse(
        Long id,
        String apelido,
        String bairro,
        String cidade,
        boolean principal,
        boolean receberAlertas) {

    public static LocalCardResponse de(Local local) {
        return new LocalCardResponse(
                local.getId(),
                local.getApelido(),
                local.getEndereco().getBairro(),
                local.getEndereco().getCidade(),
                local.getPrincipal(),
                local.getReceberAlertas());
    }
}