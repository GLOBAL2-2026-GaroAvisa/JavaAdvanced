package br.com.GaroAvisa.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.GaroAvisa.dto.AlterarNomeRequest;
import br.com.GaroAvisa.dto.AlterarPerfilIaRequest;
import br.com.GaroAvisa.dto.UsuarioResponse;
import br.com.GaroAvisa.exception.RecursoNaoEncontradoException;
import br.com.GaroAvisa.model.Usuario;
import br.com.GaroAvisa.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public UsuarioResponse buscar(Long usuarioId) {
        return UsuarioResponse.de(buscarUsuario(usuarioId));
    }

    @Transactional
    public UsuarioResponse alterarNome(Long usuarioId, AlterarNomeRequest req) {
        Usuario usuario = buscarUsuario(usuarioId);
        usuario.setNome(req.nome());
        return UsuarioResponse.de(usuario);
    }

    @Transactional
    public UsuarioResponse alterarPerfilIa(Long usuarioId, AlterarPerfilIaRequest req) {
        Usuario usuario = buscarUsuario(usuarioId);
        usuario.setPerfilIa(req.perfilIa());

        // Feature 23: quando existir o cache da Explicação do Dia,
        // apagar aqui os registros deste usuário

        return UsuarioResponse.de(usuario);
    }

    private Usuario buscarUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
    }
}