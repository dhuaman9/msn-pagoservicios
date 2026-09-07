package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pe.financiera.framework.common.retrofit.RetrofitRestClient;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.V2OperationRestClient;

@Configuration
@ConfigurationProperties(prefix = "services-api.core.operation")
@Slf4j
public class V2OperationRestClientConfig extends RetrofitRestClient {

    @Value("${appName}")
    private String appName;

    @Bean
    public V2OperationRestClient v2OperationRestClient(final ObjectMapper objectMapper) {
        log.info("Creating V2 operation endpoint with local config");
        return this.buildRestClient(appName, objectMapper).create(V2OperationRestClient.class);
    }
}
