package pe.financiera.bs.pagoservicios.service_payment.domain.model;

import java.math.BigDecimal;

import lombok.Builder;


@Builder
public record AutorizacionCommand(
		String accountUid, String customerUid, String operationNumber, BigDecimal amount, String customerDocumentType,
		String customerDocumentNumber, String customerFullName, OperationType operationType, String accountNumber,
		String canal, String recipientName, String clientId,
		/**
		 * Detalle de la operacion; en prod viaja como "reason" y termina en customStr2.
		 */
		String reason) {
}
