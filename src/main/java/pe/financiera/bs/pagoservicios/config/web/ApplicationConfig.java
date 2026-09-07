package pe.financiera.bs.pagoservicios.config.web;

import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ComponentScan(basePackages = {"pe.financiera.bs.pagoservicios", "pe.financieraoh.framework.logging",
		"pe.financieraoh.framework.auditoria.producer"}, excludeFilters = {
				@ComponentScan.Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
				@ComponentScan.Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class)})
@EntityScan({"pe.financiera.bs.pagoservicios.service_payment.infrastructure.persistence.entity"})
@EnableJpaRepositories({"pe.financiera.bs.pagoservicios.service_payment"})
public class ApplicationConfig {

	@Bean
	static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}


}
