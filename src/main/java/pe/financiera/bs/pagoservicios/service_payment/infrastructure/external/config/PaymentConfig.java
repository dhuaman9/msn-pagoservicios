package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.decorator.AuditPaymentDecorator;
import pe.financiera.bs.pagoservicios.service_payment.application.service.PayServiceInteractor;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.in.GetBillsUseCase;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.in.PayServiceUseCase;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.AuditoriaPort;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.ExternalPaymentProvider;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.OperationPort;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.ProductoPort;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.RecipientRepositoryPort;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.ServiceRepositoryPort;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.TrxOhPayPort;

@Configuration
public class PaymentConfig {

	@Bean
	public PayServiceUseCase payServiceUseCase(ProductoPort productoPort, TrxOhPayPort trxOhPayPort,
			OperationPort operationPort, ExternalPaymentProvider externalPaymentProvider,
			GetBillsUseCase getBillsUseCase, ServiceRepositoryPort serviceRepositoryPort,
			RecipientRepositoryPort recipientRepositoryPort, ObjectMapper objectMapper, AuditoriaPort auditPort) {

		PayServiceUseCase realInteractor = new PayServiceInteractor(productoPort, trxOhPayPort, operationPort,
				externalPaymentProvider, getBillsUseCase, serviceRepositoryPort, recipientRepositoryPort, objectMapper);


		return new AuditPaymentDecorator(realInteractor, auditPort);
	}
}
