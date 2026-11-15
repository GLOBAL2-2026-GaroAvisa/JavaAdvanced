package br.com.GaroAvisa.mensageria;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class DespertadorClima {

    private final RestClient restClient;

    public DespertadorClima(@Value("${garoavisa.clima.url}") String url) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(Duration.ofSeconds(5));
        fabrica.setReadTimeout(Duration.ofSeconds(60));

        this.restClient = RestClient.builder()
                .baseUrl(url)
                .requestFactory(fabrica)
                .build();
    }

    // No plano gratuito do Render o Clima dorme apos 15 min.
    // Esta chamada so serve para acorda-lo; a resposta nao importa.
    public void acordar() {
        CompletableFuture.runAsync(() -> {
            try {
                restClient.get().uri("/").retrieve().body(String.class);
                log.info("Servico de Clima acordado");
            } catch (Exception e) {
                log.warn("Nao foi possivel acordar o Servico de Clima: {}", e.getMessage());
            }
        });
    }
}