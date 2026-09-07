package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pe.financiera.framework.common.retrofit.RetrofitRestClient;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.V2HomeRestClient;

@Configuration
@ConfigurationProperties(prefix = "services-api.ux.home")
@Slf4j
public class V2HomeRestClientConfig extends RetrofitRestClient {

    @Value("${appName}")
    private String appName;

    @Bean
    public V2HomeRestClient v2HomeRestClient(final ObjectMapper objectMapper) {
        return this.buildRestClient(appName, objectMapper).create(V2HomeRestClient.class);
    }
}
