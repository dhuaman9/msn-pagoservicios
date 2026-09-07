package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import pe.financiera.framework.common.retrofit.RetrofitRestClient;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.ProductoRestClient;

@Configuration
@ConfigurationProperties(prefix = "services-api.bs.producto")
@Slf4j
public class ProductoRestClientConfig extends RetrofitRestClient {

    @Value("${appName}")
    private String appName;

    @Bean
    public ProductoRestClient productoRestClient(final ObjectMapper objectMapper) {
        return this.buildRestClient(appName, objectMapper).create(ProductoRestClient.class);
    }
}
