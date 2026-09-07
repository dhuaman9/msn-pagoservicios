package pe.financiera.bs.pagoservicios.service_payment.infrastructure.decorator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.AuditoriaCommand;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.in.CompleteBillPaymentUseCase;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.AuditoriaPort;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.ResponseWrapper;

@Slf4j
@RequiredArgsConstructor
public class AuditCompleteBillPaymentDecorator implements CompleteBillPaymentUseCase {

	public static final String SERVICIO_COMPLETAR_PAGO = "Completar Pago";

	public static final String EVENTO_COMPLETAR_PAGO = "Completar Pago";

	private final CompleteBillPaymentUseCase completeBillPaymentUseCase;

	private final AuditoriaPort auditoriaPort;

	@Override
	public void execute(CompleteBillPaymentCommand command) {
		ResponseWrapper<CompleteBillPaymentCommand> auditResponse = new ResponseWrapper<>();

		try {
			completeBillPaymentUseCase.execute(command);
			handleResponse(auditResponse, command);

		} catch (Exception ex) {
			handleErrorResponse(auditResponse, command, ex);
			throw ex;

		} finally {
			processAudit(command, auditResponse);
		}
	}

	private void handleResponse(ResponseWrapper<CompleteBillPaymentCommand> auditResponse,
			CompleteBillPaymentCommand command) {
		if (command.success()) {
			auditResponse.setDataOK(command);
		} else {
			auditResponse.setError("Error al completar el pago", -1);
			auditResponse.setData(command);
		}
	}

	private void handleErrorResponse(ResponseWrapper<CompleteBillPaymentCommand> auditResponse,
			CompleteBillPaymentCommand command, Exception ex) {
		auditResponse.setError(ex.getMessage(), -1);
		auditResponse.setData(command);
	}

	private void processAudit(CompleteBillPaymentCommand command,
			ResponseWrapper<CompleteBillPaymentCommand> auditResponse) {
		try {
			AuditoriaCommand auditoriaCommand = AuditoriaCommand.builder().codigoOperacion(command.operationId())
					.codInterno(command.userId()).servicio(SERVICIO_COMPLETAR_PAGO).evento(EVENTO_COMPLETAR_PAGO)
					.build();

			auditoriaPort.procesarAuditoria(null, auditResponse, auditoriaCommand);

		} catch (Exception ex) {
			log.error("Error al procesar auditoria de completar pago para la operación {}: {}",
					command != null ? command.operationNumber() : "N/A", ex.getMessage(), ex);
		}
	}
}
