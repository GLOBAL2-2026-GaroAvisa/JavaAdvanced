package br.com.GaroAvisa.dto;

import br.com.GaroAvisa.model.Usuario;

public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        String perfilIa) {

    public static UsuarioResponse de(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPerfilIa());
    }
}