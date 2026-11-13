package br.com.GaroAvisa.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.GaroAvisa.dto.AlterarNomeRequest;
import br.com.GaroAvisa.dto.AlterarPerfilIaRequest;
import br.com.GaroAvisa.dto.AlterarSenhaRequest;
import br.com.GaroAvisa.dto.UsuarioResponse;
import br.com.GaroAvisa.exception.RecursoNaoEncontradoException;
import br.com.GaroAvisa.exception.SenhaInvalidaException;
import br.com.GaroAvisa.model.Usuario;
import br.com.GaroAvisa.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

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
    
    @Transactional
    public void alterarSenha(Long usuarioId, AlterarSenhaRequest req) {
        Usuario usuario = buscarUsuario(usuarioId);

        if (!passwordEncoder.matches(req.senhaAtual(), usuario.getSenha())) {
            throw new SenhaInvalidaException("Senha atual incorreta.");
        }

        if (req.novaSenha().equals(req.senhaAtual())) {
            throw new SenhaInvalidaException("A nova senha deve ser diferente da atual.");
        }

        usuario.setSenha(passwordEncoder.encode(req.novaSenha()));
    }
    
    // Feature 69
    @Transactional
    public void excluir(Long usuarioId) {
        Usuario usuario = buscarUsuario(usuarioId);
        usuarioRepository.delete(usuario);
    }

    private Usuario buscarUsuario(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
    }
}