package br.com.GaroAvisa.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "garoavisa.jwt")
public record JwtProperties(String segredo, Duration expiracao) {
}