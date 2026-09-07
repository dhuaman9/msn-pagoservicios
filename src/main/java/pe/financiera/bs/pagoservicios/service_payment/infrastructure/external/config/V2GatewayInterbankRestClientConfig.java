package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.config.IbkRetrofitErrorInterceptor;
import pe.financiera.framework.common.retrofit.RetrofitRestClient;
import pe.financiera.bs.pagoservicios.config.interceptor.MdcPropagationInterceptor;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.V2GatewayInterbankRestClient;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.concurrent.TimeUnit;

@Configuration
@ConfigurationProperties(prefix = "services-api.gateway.interbank")
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
public class V2GatewayInterbankRestClientConfig extends RetrofitRestClient {

	@Value("${appName}")
	private String appName;

	@Bean
	public V2GatewayInterbankRestClient v2GatewayInterbankRestClient(final ObjectMapper objectMapper) {

		Interceptor urlLoggingInterceptor = chain -> {
			Request request = chain.request();
			log.info("URL Completa a invocar: {} {}", request.method(), request.url());
			return chain.proceed(request);
		};

		OkHttpClient httpClient = new OkHttpClient.Builder().connectTimeout(this.connectTimeout, TimeUnit.MILLISECONDS)
				.readTimeout(this.readTimeout, TimeUnit.MILLISECONDS)
				.writeTimeout(this.writeTimeout, TimeUnit.MILLISECONDS).addInterceptor(new MdcPropagationInterceptor())
				.addInterceptor(new IbkRetrofitErrorInterceptor()).addInterceptor(urlLoggingInterceptor).build();

		return new Retrofit.Builder().client(httpClient).baseUrl(this.baseUrl)
				.addConverterFactory(JacksonConverterFactory.create(objectMapper)).build()
				.create(V2GatewayInterbankRestClient.class);
	}
}
