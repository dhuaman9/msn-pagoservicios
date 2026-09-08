package pe.financiera.bs.pagoservicios.service_payment.infrastructure.decorator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.AuditoriaCommand;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.PaymentExecutionResult;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.PaymentRequest;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.RechargeRequest;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.in.PayRechargeUseCase;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.AuditoriaPort;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.ResponseWrapper;

@Slf4j
@RequiredArgsConstructor
public class AuditRechargeDecorator implements PayRechargeUseCase {

	public static final String SERVICIO_RECARGA = "Recarga de celular";
	public static final String EVENTO_RECARGA = "Recarga de celular";

	private final PayRechargeUseCase payRechargeUseCase;

	private final AuditoriaPort auditoriaPort;

	@Override
	public PaymentExecutionResult execute(RechargeRequest request) {

		ResponseWrapper<PaymentExecutionResult> auditResponse = new ResponseWrapper<>();
		PaymentExecutionResult result;
		String codigoOperacion = "";

		try {
			result = payRechargeUseCase.execute(request);
			auditResponse.setDataOK(result);
			codigoOperacion = result.getOperationNumber();
		} catch (Exception ex) {
			auditResponse.setError(ex.getMessage(), -1);
			throw ex;

		} finally {

			AuditoriaCommand command = AuditoriaCommand.builder().codInterno(request.getCodInterno())
					.codigoOperacion(codigoOperacion).headers(request.getHeadersAsJson()).servicio(SERVICIO_RECARGA)
					.evento(EVENTO_RECARGA).build();

			auditoriaPort.procesarAuditoria(request, auditResponse, command);

		}

		return result;
	}
}
