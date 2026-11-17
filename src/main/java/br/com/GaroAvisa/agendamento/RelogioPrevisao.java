package br.com.GaroAvisa.agendamento;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import br.com.GaroAvisa.evento.EnderecoParaPrevisao;
import br.com.GaroAvisa.mensageria.PrevisaoSolicitadaPublisher;
import br.com.GaroAvisa.repository.AlertaClimaRepository;
import br.com.GaroAvisa.repository.EnderecoRepository;
import br.com.GaroAvisa.repository.ExplicacaoDiaRepository;
import br.com.GaroAvisa.repository.MudancaPrevisaoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class RelogioPrevisao {

    private final EnderecoRepository enderecoRepository;
    private final PrevisaoSolicitadaPublisher publisher;
    private final MudancaPrevisaoRepository mudancaRepository;
    private final AlertaClimaRepository alertaRepository;
    private final ExplicacaoDiaRepository explicacaoRepository;
    
    // 1 minuto depois de subir e, depois, de hora em hora
    @Transactional
    @Scheduled(initialDelay = 1, fixedRate = 60, timeUnit = TimeUnit.MINUTES)
    public void solicitarPrevisoes() {
        // Limpeza do que se refere a dias que ja passaram
        LocalDate hoje = LocalDate.now(ZoneId.of("America/Sao_Paulo"));
        
        alertaRepository.apagarAntesDe(hoje);
    		mudancaRepository.apagarAntesDe(LocalDate.now(ZoneId.of("America/Sao_Paulo")));
            explicacaoRepository.apagarAntesDe(hoje);
        
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