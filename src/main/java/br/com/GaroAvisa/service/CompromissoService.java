package br.com.GaroAvisa.service;

import java.time.LocalDate;
import java.time.ZoneId;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.GaroAvisa.dto.CompromissoResponse;
import br.com.GaroAvisa.dto.CriarCompromissoRequest;
import br.com.GaroAvisa.exception.DataNoPassadoException;
import br.com.GaroAvisa.exception.RecursoNaoEncontradoException;
import br.com.GaroAvisa.model.Compromisso;
import br.com.GaroAvisa.model.Local;
import br.com.GaroAvisa.repository.CompromissoRepository;
import br.com.GaroAvisa.repository.LocalRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompromissoService {

    private final CompromissoRepository compromissoRepository;
    private final LocalRepository localRepository;

    // ---------- POST /locais/{localId}/compromissos ----------
    @Transactional
    public CompromissoResponse criar(Long usuarioId, Long localId, CriarCompromissoRequest requisicao) {

        // 1. O local precisa ser do usuário
        Local local = localRepository.findByIdAndUsuarioId(usuarioId, localId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Local não encontrado"));

        // 2. Data no passado não é permitida (no fuso do local)
        validarData(requisicao.data(), local);

        // 3. Grava
        Compromisso compromisso = new Compromisso(local, requisicao.nome().trim(), requisicao.data());
        compromissoRepository.save(compromisso);

        return CompromissoResponse.de(compromisso);
    }

    // ---------- Auxiliares ----------

    private void validarData(LocalDate data, Local local) {
        LocalDate hoje = LocalDate.now(ZoneId.of(local.getEndereco().getFuso()));

        if (data.isBefore(hoje)) {
            throw new DataNoPassadoException();
        }
    }
}