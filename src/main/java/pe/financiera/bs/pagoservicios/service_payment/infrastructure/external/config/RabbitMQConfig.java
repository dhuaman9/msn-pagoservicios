package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.config;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pe.financieraoh.framework.auditoria.producer.util.JacksonMessageConverter;

@Configuration
public class RabbitMQConfig {

    @Bean(name= "rabbitJsonMessageConverter")
    public Jackson2JsonMessageConverter jackson2JsonSmsConverter() {
        return new JacksonMessageConverter();
    }
}
