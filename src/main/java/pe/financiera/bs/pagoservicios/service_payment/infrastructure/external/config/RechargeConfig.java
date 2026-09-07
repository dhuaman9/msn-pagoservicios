package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import pe.financiera.bs.pagoservicios.service_payment.infrastructure.decorator.AuditRechargeDecorator;
import pe.financiera.bs.pagoservicios.service_payment.application.service.PayRechargeInteractor;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.in.PayRechargeUseCase;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.AuditoriaPort;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.ExternalPaymentProvider;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.OperationPort;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.ProductoPort;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.RecipientRepositoryPort;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.ServiceRepositoryPort;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.TrxOhPayPort;

@Configuration
public class RechargeConfig {

	@Bean
	public PayRechargeUseCase payRechargeUseCase(ProductoPort productoPort, TrxOhPayPort trxOhPayPort,
			OperationPort operationPort, ExternalPaymentProvider externalPaymentProvider,
			ServiceRepositoryPort serviceRepositoryPort, RecipientRepositoryPort recipientRepositoryPort,
			ObjectMapper objectMapper, AuditoriaPort auditPort) {

		PayRechargeUseCase realInteractor = new PayRechargeInteractor(productoPort, trxOhPayPort, operationPort,
				externalPaymentProvider, serviceRepositoryPort, recipientRepositoryPort, objectMapper);

		return new AuditRechargeDecorator(realInteractor, auditPort);
	}
}
