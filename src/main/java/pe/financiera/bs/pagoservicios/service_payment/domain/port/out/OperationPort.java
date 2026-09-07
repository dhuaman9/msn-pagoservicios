package pe.financiera.bs.pagoservicios.service_payment.domain.port.out;

import pe.financiera.bs.pagoservicios.service_payment.domain.model.OperationResponse;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Transaction;
import java.math.BigDecimal;

public interface OperationPort {

	Transaction getTransactionByOperationNumber(String operationNumber);

	OperationResponse getOperationById(String operationId);

	void updateTransactionStatus(String transactionId, String status, String userId);

	void updateOperationStatus(String operationId, String status, String userId);

	OperationResponse createOperation(String userId, String accountId, BigDecimal amount, String type,
			String destination, String originName, String destinationName, String description, String detail,
			String additionalData);

	Transaction createTransaction(String operationId, String userId, String type, String platform, BigDecimal amount,
			String operationNumber);
}
