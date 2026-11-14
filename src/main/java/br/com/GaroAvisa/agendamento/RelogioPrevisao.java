package br.com.GaroAvisa.agendamento;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import br.com.GaroAvisa.evento.EnderecoParaPrevisao;
import br.com.GaroAvisa.mensageria.PrevisaoSolicitadaPublisher;
import br.com.GaroAvisa.repository.EnderecoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RelogioPrevisao {

    private final EnderecoRepository enderecoRepository;
    private final PrevisaoSolicitadaPublisher publisher;

    // 1 minuto depois de subir e, depois, de hora em hora
    @Scheduled(initialDelay = 1, fixedRate = 60, timeUnit = TimeUnit.MINUTES)
    public void solicitarPrevisoes() {
        List<EnderecoParaPrevisao> enderecos = enderecoRepository.listarEmUsoParaPrevisao();

        if (enderecos.isEmpty()) {
            log.info("Atualização agendada: nenhum endereço em uso");
            return;
        }

        try {
            publisher.agendado(enderecos);
        } catch (Exception e) {
            // Se o RabbitMQ falhar, a próxima rodada tenta de novo
            log.warn("Falha na atualização agendada: {}", e.getMessage());
        }
    }
}