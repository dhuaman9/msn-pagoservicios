package pe.financiera.bs.pagoservicios.service_payment.infrastructure.decorator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.AuditoriaCommand;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.PaymentExecutionResult;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.PaymentRequest;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.in.PayServiceUseCase;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.AuditoriaPort;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.ResponseWrapper;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditPaymentDecoratorTest {

	private AuditPaymentDecorator decorator;

	private PayServiceUseCase payServiceUseCase;
	private AuditoriaPort auditoriaPort;

	private PaymentRequest requestMock;
	private PaymentExecutionResult resultMock;

	@BeforeEach
	void setUp() {
		payServiceUseCase = mock(PayServiceUseCase.class);
		auditoriaPort = mock(AuditoriaPort.class);
		decorator = new AuditPaymentDecorator(payServiceUseCase, auditoriaPort);

		requestMock = PaymentRequest.builder().codInterno("INT-001").recipientId("REC-001").serviceId("SVC-001")
				.clientId("CLI-001").amount(BigDecimal.valueOf(100.0)).build();

		resultMock = PaymentExecutionResult.builder().operationId("OP-001").operationNumber("OP-001-NUM")
				.amount(BigDecimal.valueOf(100.0)).amountFormat("S/ 100.00").status("SUCCESS")
				.message("Payment registered").build();
	}

	// ─────────────────────────────────────────────────────────────────────────
	// Ejecución exitosa: Verifica flujo completo con auditoría OK
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void execute_whenPaymentSuccessful_shouldCallUseCaseAndProcessAuditWithOK() {
		// Arrange
		when(payServiceUseCase.execute(requestMock)).thenReturn(resultMock);
		doNothing().when(auditoriaPort).procesarAuditoria(any(PaymentRequest.class), any(ResponseWrapper.class),
				any(AuditoriaCommand.class));

		// Act
		PaymentExecutionResult result = decorator.execute(requestMock);

		// Assert
		assertEquals(resultMock.getOperationNumber(), result.getOperationNumber());
		assertEquals("SUCCESS", result.getStatus());
		verify(payServiceUseCase).execute(requestMock);
		verify(auditoriaPort).procesarAuditoria(eq(requestMock), any(ResponseWrapper.class),
				any(AuditoriaCommand.class));
	}

	@Test
	void execute_whenPaymentSuccessful_shouldExtractOperationNumberFromResult() {
		// Arrange
		when(payServiceUseCase.execute(requestMock)).thenReturn(resultMock);
		doNothing().when(auditoriaPort).procesarAuditoria(any(PaymentRequest.class), any(ResponseWrapper.class),
				any(AuditoriaCommand.class));

		// Act
		decorator.execute(requestMock);

		// Assert - Verify that operationNumber from result is used in audit
		verify(auditoriaPort).procesarAuditoria(eq(requestMock), any(ResponseWrapper.class),
				any(AuditoriaCommand.class));
	}

	@Test
	void execute_whenPaymentSuccessful_shouldSetAuditResponseAsDataOK() {
		// Arrange
		when(payServiceUseCase.execute(requestMock)).thenReturn(resultMock);
		doNothing().when(auditoriaPort).procesarAuditoria(any(PaymentRequest.class), any(ResponseWrapper.class),
				any(AuditoriaCommand.class));

		// Act
		PaymentExecutionResult result = decorator.execute(requestMock);

		// Assert - auditResponse.setDataOK(result) is called internally
		assertEquals(resultMock.getOperationId(), result.getOperationId());
		verify(auditoriaPort).procesarAuditoria(eq(requestMock), any(ResponseWrapper.class),
				any(AuditoriaCommand.class));
	}

	@Test
	void execute_whenPaymentSuccessful_shouldProcessAuditInFinallyBlock() {
		// Arrange
		when(payServiceUseCase.execute(requestMock)).thenReturn(resultMock);
		doNothing().when(auditoriaPort).procesarAuditoria(any(PaymentRequest.class), any(ResponseWrapper.class),
				any(AuditoriaCommand.class));

		// Act
		decorator.execute(requestMock);

		// Assert - Verify audit is processed even on success
		verify(auditoriaPort).procesarAuditoria(any(PaymentRequest.class), any(ResponseWrapper.class),
				any(AuditoriaCommand.class));
	}

	// ─────────────────────────────────────────────────────────────────────────
	// Excepción durante la ejecución: Se captura, se configura error y se propaga
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void execute_whenUseCaseThrowsException_shouldCaptureErrorAndRethrowException() {
		// Arrange
		RuntimeException testException = new RuntimeException("Payment processing failed");
		doThrow(testException).when(payServiceUseCase).execute(requestMock);
		doNothing().when(auditoriaPort).procesarAuditoria(any(PaymentRequest.class), any(ResponseWrapper.class),
				any(AuditoriaCommand.class));

		// Act & Assert
		RuntimeException thrown = assertThrows(RuntimeException.class, () -> decorator.execute(requestMock));
		assertEquals("Payment processing failed", thrown.getMessage());

		// Verify use case was called
		verify(payServiceUseCase).execute(requestMock);
	}

	@Test
	void execute_whenUseCaseThrowsException_shouldConfigureAuditResponseWithError() {
		// Arrange
		RuntimeException testException = new RuntimeException("Insufficient funds");
		doThrow(testException).when(payServiceUseCase).execute(requestMock);
		doNothing().when(auditoriaPort).procesarAuditoria(any(PaymentRequest.class), any(ResponseWrapper.class),
				any(AuditoriaCommand.class));

		// Act & Assert
		assertThrows(RuntimeException.class, () -> decorator.execute(requestMock));

		// Verify that auditResponse.setError() was called with exception message
		verify(auditoriaPort).procesarAuditoria(eq(requestMock), any(ResponseWrapper.class),
				any(AuditoriaCommand.class));
	}

	@Test
	void execute_whenExceptionOccurs_shouldExecuteAuditInFinallyBlockBeforePropagatingException() {
		// Arrange
		IllegalArgumentException testException = new IllegalArgumentException("Invalid payment request");
		doThrow(testException).when(payServiceUseCase).execute(requestMock);
		doNothing().when(auditoriaPort).procesarAuditoria(any(PaymentRequest.class), any(ResponseWrapper.class),
				any(AuditoriaCommand.class));

		// Act & Assert
		assertThrows(IllegalArgumentException.class, () -> decorator.execute(requestMock));

		// Verify that audit was still called in finally block
		verify(auditoriaPort).procesarAuditoria(any(PaymentRequest.class), any(ResponseWrapper.class),
				any(AuditoriaCommand.class));
	}

	@Test
	void execute_whenExceptionOccurs_shouldNotSetCodigoOperacionFromFailedResult() {
		// Arrange
		RuntimeException testException = new RuntimeException("Transaction denied");
		doThrow(testException).when(payServiceUseCase).execute(requestMock);
		doNothing().when(auditoriaPort).procesarAuditoria(any(PaymentRequest.class), any(ResponseWrapper.class),
				any(AuditoriaCommand.class));

		// Act & Assert
		assertThrows(RuntimeException.class, () -> decorator.execute(requestMock));

		// Verify audit is called with empty codigoOperacion when exception occurs
		verify(auditoriaPort).procesarAuditoria(eq(requestMock), any(ResponseWrapper.class),
				any(AuditoriaCommand.class));
	}
}
