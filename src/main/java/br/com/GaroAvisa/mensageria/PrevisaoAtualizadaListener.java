package br.com.GaroAvisa.mensageria;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import br.com.GaroAvisa.evento.PrevisaoAtualizadaEvento;
import br.com.GaroAvisa.service.PrevisaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PrevisaoAtualizadaListener {

    private final PrevisaoService previsaoService;

    @RabbitListener(queues = Mensageria.FILA_PREVISAO_ATUALIZADA)
    public void receber(PrevisaoAtualizadaEvento evento) {
        previsaoService.salvar(evento);

        log.info("Previsão guardada: endereço {}, {} horas, {} dias",
                evento.enderecoId(), evento.horas().size(), evento.dias().size());

        // Feature 21: aqui vai entrar a comparacao com a previsao anterior
        // Epic 5: aqui vao entrar a geracao de alertas e as notificacoes
    }
}