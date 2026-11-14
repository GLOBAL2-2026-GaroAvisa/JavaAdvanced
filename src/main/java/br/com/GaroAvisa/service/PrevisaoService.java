package br.com.GaroAvisa.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.GaroAvisa.evento.PrevisaoAtualizadaEvento;
import br.com.GaroAvisa.model.Previsao;
import br.com.GaroAvisa.repository.PrevisaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class PrevisaoService {

    private final PrevisaoRepository previsaoRepository;
    private final ObjectMapper objectMapper;

    // Guarda a previsao do endereco, substituindo a anterior
    @Transactional
    public void salvar(PrevisaoAtualizadaEvento evento) {
        String json = paraJson(evento);

        Previsao previsao = previsaoRepository.findById(evento.enderecoId()).orElse(null);

        if (previsao == null) {
            previsaoRepository.save(new Previsao(evento.enderecoId(), json, evento.atualizadoEm()));
            return;
        }
        
        // Mensagem repetida ou atrasada (reentrega): a previsao guardada ja e igual ou mais nova
        if (!previsao.getAtualizadoEm().isBefore(evento.atualizadoEm())) {
            log.info("Previsao do endereco {} ignorada: a guardada e igual ou mais nova", evento.enderecoId());
            return;
        }

        previsao.atualizar(json, evento.atualizadoEm());
    }

    private String paraJson(PrevisaoAtualizadaEvento evento) {
        try {
            return objectMapper.writeValueAsString(evento);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Falha ao converter a previsao do endereco " + evento.enderecoId(), e);
        }
    }
}