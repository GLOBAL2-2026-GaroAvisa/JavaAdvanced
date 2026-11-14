package br.com.GaroAvisa.mensageria;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class PrevisaoSolicitadaGatilhos {

    private final PrevisaoSolicitadaPublisher publisher;

    // Só roda se a transação que criou o endereço fizer COMMIT.
    // Se houver rollback (ex.: apelido duplicado), o aviso é descartado.
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void enderecoNovo(EnderecoNovoCriado aviso) {
        try {
            publisher.localCriado(aviso.endereco());
        } catch (Exception e) {
            // O Local já foi salvo. Se o RabbitMQ falhar, a atualização
            // de hora em hora pede a previsão deste endereço depois.
            log.warn("Falha ao solicitar previsão do endereço {}: {}",
                    aviso.endereco().enderecoId(), e.getMessage());
        }
    }
}