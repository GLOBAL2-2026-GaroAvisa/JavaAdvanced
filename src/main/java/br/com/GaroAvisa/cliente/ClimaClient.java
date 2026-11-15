package br.com.GaroAvisa.cliente;

import java.math.BigDecimal;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import br.com.GaroAvisa.evento.PrevisaoAtualizadaEvento;
import br.com.GaroAvisa.exception.ServicoIndisponivelException;
import lombok.extern.slf4j.Slf4j;

// Chamada SINCRONA ao Servico de Clima, usada pela tela de Pesquisa.
// Toda a comunicacao HTTP entre os dois servicos passa por aqui:
// para migrar para OpenFeign + Eureka, so esta classe muda.
@Slf4j
@Component
public class ClimaClient {

    private final RestClient restClient;

    public ClimaClient(@Value("${garoavisa.clima.url}") String url) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofSeconds(10));
        // 60 s: no plano gratuito do Render o Clima pode estar acordando
        fabrica.setReadTimeout(Duration.ofSeconds(60));

        this.restClient = RestClient.builder()
                .baseUrl(url)
                .requestFactory(fabrica)
                .build();
    }

    public PrevisaoAtualizadaEvento buscar(BigDecimal latitude, BigDecimal longitude, String fuso) {
        try {
            PrevisaoAtualizadaEvento previsao = restClient.get()
                    .uri(uri -> uri.path("/previsao")
                            .queryParam("latitude", latitude.toPlainString())
                            .queryParam("longitude", longitude.toPlainString())
                            .queryParam("fuso", fuso)
                            .build())
                    .retrieve()
                    .body(PrevisaoAtualizadaEvento.class);

            if (previsao == null) {
                throw new IllegalStateException("resposta vazia");
            }
            return previsao;

        } catch (Exception e) {
            log.warn("Falha ao consultar o Servico de Clima: {}", e.getMessage());
            throw new ServicoIndisponivelException("Não foi possível buscar a previsão. Tente de novo.");
        }
    }
}