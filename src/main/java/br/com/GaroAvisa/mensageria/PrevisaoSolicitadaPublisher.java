package br.com.GaroAvisa.mensageria;

import java.util.List;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import br.com.GaroAvisa.evento.EnderecoParaPrevisao;
import br.com.GaroAvisa.evento.PrevisaoSolicitadaEvento;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PrevisaoSolicitadaPublisher {

    // Endereços por mensagem no pedido agendado
    private static final int TAMANHO_LOTE = 50;

    private final RabbitTemplate rabbitTemplate;

    // Gatilho 1: um Local foi criado num endereço novo
    public void localCriado(EnderecoParaPrevisao endereco) {
        publicar(Mensageria.RK_PREVISAO_SOLICITADA_LOCAL_CRIADO, List.of(endereco));
    }

    // Gatilho 2: atualização de hora em hora, em lotes
    public void agendado(List<EnderecoParaPrevisao> enderecos) {
        for (int inicio = 0; inicio < enderecos.size(); inicio += TAMANHO_LOTE) {
            int fim = Math.min(inicio + TAMANHO_LOTE, enderecos.size());
            publicar(Mensageria.RK_PREVISAO_SOLICITADA_AGENDADA, enderecos.subList(inicio, fim));
        }
    }

    private void publicar(String routingKey, List<EnderecoParaPrevisao> enderecos) {
        PrevisaoSolicitadaEvento evento = new PrevisaoSolicitadaEvento(List.copyOf(enderecos));
        rabbitTemplate.convertAndSend(Mensageria.EXCHANGE, routingKey, evento);
        log.info("Previsão solicitada ({}): {} endereço(s)", routingKey, enderecos.size());
    }
}