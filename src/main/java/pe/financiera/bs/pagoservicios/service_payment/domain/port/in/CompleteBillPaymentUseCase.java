package pe.financiera.bs.pagoservicios.service_payment.domain.port.in;

import java.math.BigDecimal;
import java.util.Map;

public interface CompleteBillPaymentUseCase {
	void execute(CompleteBillPaymentCommand command);

	record CompleteBillPaymentCommand(String operationNumber, String transactionId, boolean success, BigDecimal amount,
			String userId, String operationId, String errorCode, Map<String, Object> customProperties) {
	}
}
