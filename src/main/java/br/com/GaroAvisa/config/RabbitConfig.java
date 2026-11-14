package br.com.GaroAvisa.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.GaroAvisa.mensageria.Mensageria;

@Configuration
public class RabbitConfig {

    // Exchange de tópicos compartilhada com o Clima.
    // Precisa ter EXATAMENTE os mesmos parâmetros da declaração no Clima.
    @Bean
    TopicExchange garoavisaEventos() {
        return new TopicExchange(Mensageria.EXCHANGE, true, false);
    }

    // Objeto <-> JSON. Usado pelo RabbitTemplate ao publicar
    // e, no E2, pelo @RabbitListener ao consumir
    @Bean
    MessageConverter conversorJson() {
        JacksonJsonMessageConverter conversor = new JacksonJsonMessageConverter();
        conversor.setAlwaysConvertToInferredType(true);
        return conversor;
    }
}