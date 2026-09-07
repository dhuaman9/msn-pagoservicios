package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pe.financiera.framework.common.retrofit.RetrofitRestClient;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.TrxOhPayRestClient;

@Configuration
@ConfigurationProperties(prefix = "services-api.bs.trxohpay")
@Slf4j
public class TrxOhPayRestClientConfig extends RetrofitRestClient {

    @Value("${appName}")
    private String appName;

    @Bean
    public TrxOhPayRestClient trxOhPayRestClient(final ObjectMapper objectMapper) {
        log.info("Creating business authorization URL: {}", this.baseUrl);
        return this.buildRestClient(appName, objectMapper).create(TrxOhPayRestClient.class);
    }
}
