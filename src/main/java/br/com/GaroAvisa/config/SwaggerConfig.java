package br.com.GaroAvisa.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "GaroAvisa API",
                version = "0.1.0",
                description = "Backend do GaroAvisa: clima, alertas e dicas para os locais salvos do usuário"),
        security = @SecurityRequirement(name = SwaggerConfig.BEARER))
@SecurityScheme(
        name = SwaggerConfig.BEARER,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT")
public class SwaggerConfig {

    public static final String BEARER = "bearerAuth";
}