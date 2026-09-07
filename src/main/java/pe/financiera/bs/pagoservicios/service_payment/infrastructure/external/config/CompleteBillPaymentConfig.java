package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import pe.financiera.bs.pagoservicios.service_payment.application.service.CompleteBillPaymentInteractor;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.in.CompleteBillPaymentUseCase;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.AuditoriaPort;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.OperationPort;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.ProductoPort;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.TrxOhPayPort;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.decorator.AuditCompleteBillPaymentDecorator;

@Configuration
public class CompleteBillPaymentConfig {

	@Bean
	public CompleteBillPaymentUseCase completeBillPaymentUseCase(TrxOhPayPort trxOhPayPort, OperationPort operationPort,
			ProductoPort productoPort, AuditoriaPort auditPort) {

		CompleteBillPaymentUseCase realInteractor = new CompleteBillPaymentInteractor(trxOhPayPort, operationPort,
				productoPort);

		return new AuditCompleteBillPaymentDecorator(realInteractor, auditPort);
	}
}
