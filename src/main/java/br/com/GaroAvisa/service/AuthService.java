package br.com.GaroAvisa.service;

import java.util.Locale;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.GaroAvisa.dto.CadastroRequest;
import br.com.GaroAvisa.dto.LoginRequest;
import br.com.GaroAvisa.dto.TokenResponse;
import br.com.GaroAvisa.exception.CredenciaisInvalidasException;
import br.com.GaroAvisa.exception.EmailJaCadastradoException;
import br.com.GaroAvisa.model.Usuario;
import br.com.GaroAvisa.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    @Transactional
    public TokenResponse cadastrar(CadastroRequest requisicao) {
        String email = normalizarEmail(requisicao.email());

        if (usuarioRepository.existsByEmail(email)) {
            throw new EmailJaCadastradoException();
        }

        Usuario usuario = new Usuario();
        usuario.setNome(requisicao.nome().trim());
        usuario.setEmail(email);
        usuario.setSenha(passwordEncoder.encode(requisicao.senha()));

        try {
            usuarioRepository.saveAndFlush(usuario);
        } catch (DataIntegrityViolationException ex) {
            throw new EmailJaCadastradoException();
        }

        return tokenService.gerar(usuario);
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest requisicao) {
        Usuario usuario = usuarioRepository.findByEmail(normalizarEmail(requisicao.email()))
                .filter(u -> passwordEncoder.matches(requisicao.senha(), u.getSenha()))
                .orElseThrow(CredenciaisInvalidasException::new);

        return tokenService.gerar(usuario);
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}