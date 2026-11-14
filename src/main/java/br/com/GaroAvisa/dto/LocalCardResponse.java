package br.com.GaroAvisa.dto;

import br.com.GaroAvisa.model.Local;

public record LocalCardResponse(
        Long id,
        String apelido,
        String nome,
        String cidade,
        boolean principal,
        boolean receberAlertas) {

    public static LocalCardResponse de(Local local) {
        return new LocalCardResponse(
                local.getId(),
                local.getApelido(),
                local.getEndereco().getNome(),
                local.getEndereco().getCidade(),
                local.getPrincipal(),
                local.getReceberAlertas());
    }
}