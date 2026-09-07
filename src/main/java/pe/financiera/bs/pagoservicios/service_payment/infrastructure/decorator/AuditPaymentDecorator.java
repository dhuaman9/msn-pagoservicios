package pe.financiera.bs.pagoservicios.service_payment.infrastructure.decorator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.AuditoriaCommand;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.PaymentExecutionResult;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.PaymentRequest;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.in.PayServiceUseCase;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.AuditoriaPort;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.ResponseWrapper;

@Slf4j
@RequiredArgsConstructor
public class AuditPaymentDecorator implements PayServiceUseCase {

	public static final String SERVICIO_PAGO_SERVICIOS = "Pago Servicios";

	public static final String EVENTO_PAGO_SERVICIOS = "Pago Servicios";

	private final PayServiceUseCase payServiceUseCase;

	private final AuditoriaPort auditoriaPort;

	@Override
	public PaymentExecutionResult execute(PaymentRequest request) {

		ResponseWrapper<PaymentExecutionResult> auditResponse = new ResponseWrapper<>();
		PaymentExecutionResult result;
		String codigoOperacion = "";

		try {
			result = payServiceUseCase.execute(request);
			codigoOperacion = result.getOperationNumber();
			auditResponse.setDataOK(result);
		} catch (Exception ex) {
			auditResponse.setError(ex.getMessage(), -1);
			throw ex;

		} finally {

			AuditoriaCommand command = AuditoriaCommand.builder().codInterno(request.getCodInterno())
					.codigoOperacion(codigoOperacion).headers(request.getHeadersAsJson())
					.servicio(SERVICIO_PAGO_SERVICIOS).evento(EVENTO_PAGO_SERVICIOS).build();

			auditoriaPort.procesarAuditoria(request, auditResponse, command);

		}

		return result;
	}
}
