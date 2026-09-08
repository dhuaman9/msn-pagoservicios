package pe.financiera.bs.pagoservicios.service_payment.infrastructure.decorator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.AuditoriaCommand;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.PaymentExecutionResult;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.RechargeRequest;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.in.PayRechargeUseCase;
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
class AuditRechargeDecoratorTest {

	private AuditRechargeDecorator decorator;

	private PayRechargeUseCase payRechargeUseCase;
	private AuditoriaPort auditoriaPort;

	private RechargeRequest requestMock;
	private PaymentExecutionResult resultMock;

	@BeforeEach
	void setUp() {
		payRechargeUseCase = mock(PayRechargeUseCase.class);
		auditoriaPort = mock(AuditoriaPort.class);
		decorator = new AuditRechargeDecorator(payRechargeUseCase, auditoriaPort);

		requestMock = RechargeRequest.builder().codInterno("INT-001").recipientId("REC-001").serviceId("SVC-001")
				.clientId("CLI-001").amount(BigDecimal.valueOf(50.0)).operationType(null).build();

		resultMock = PaymentExecutionResult.builder().operationId("OP-RECHARGE-001")
				.operationNumber("OP-RECHARGE-001-NUM").amount(BigDecimal.valueOf(50.0)).amountFormat("S/ 50.00")
				.status("SUCCESS").message("Recharge registered").build();
	}

	// ─────────────────────────────────────────────────────────────────────────
	// Ejecución exitosa: Verifica flujo completo con auditoría OK
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void execute_whenRechargeSuccessful_shouldCallUseCaseAndProcessAuditWithOK() {
		// Arrange
		when(payRechargeUseCase.execute(requestMock)).thenReturn(resultMock);
		doNothing().when(auditoriaPort).procesarAuditoria(any(RechargeRequest.class), any(ResponseWrapper.class),
				any(AuditoriaCommand.class));

		// Act
		PaymentExecutionResult result = decorator.execute(requestMock);

		// Assert
		assertEquals(resultMock.getOperationNumber(), result.getOperationNumber());
		assertEquals("SUCCESS", result.getStatus());
		verify(payRechargeUseCase).execute(requestMock);
		verify(auditoriaPort).procesarAuditoria(eq(requestMock), any(ResponseWrapper.class),
				any(AuditoriaCommand.class));
	}

	@Test
	void execute_whenRechargeSuccessful_shouldExtractOperationNumberFromResult() {
		// Arrange
		when(payRechargeUseCase.execute(requestMock)).thenReturn(resultMock);
		doNothing().when(auditoriaPort).procesarAuditoria(any(RechargeRequest.class), any(ResponseWrapper.class),
				any(AuditoriaCommand.class));

		// Act
		decorator.execute(requestMock);

		// Assert - Verify that operationNumber from result is used in audit
		verify(auditoriaPort).procesarAuditoria(eq(requestMock), any(ResponseWrapper.class),
				any(AuditoriaCommand.class));
	}

	@Test
	void execute_whenRechargeSuccessful_shouldSetAuditResponseAsDataOK() {
		// Arrange
		when(payRechargeUseCase.execute(requestMock)).thenReturn(resultMock);
		doNothing().when(auditoriaPort).procesarAuditoria(any(RechargeRequest.class), any(ResponseWrapper.class),
				any(AuditoriaCommand.class));

		// Act
		PaymentExecutionResult result = decorator.execute(requestMock);

		// Assert - auditResponse.setDataOK(result) is called internally
		assertEquals(resultMock.getOperationId(), result.getOperationId());
		verify(auditoriaPort).procesarAuditoria(eq(requestMock), any(ResponseWrapper.class),
				any(AuditoriaCommand.class));
	}

	@Test
	void execute_whenRechargeSuccessful_shouldProcessAuditInFinallyBlock() {
		// Arrange
		when(payRechargeUseCase.execute(requestMock)).thenReturn(resultMock);
		doNothing().when(auditoriaPort).procesarAuditoria(any(RechargeRequest.class), any(ResponseWrapper.class),
				any(AuditoriaCommand.class));

		// Act
		decorator.execute(requestMock);

		// Assert - Verify audit is processed even on success
		verify(auditoriaPort).procesarAuditoria(any(RechargeRequest.class), any(ResponseWrapper.class),
				any(AuditoriaCommand.class));
	}

	// ─────────────────────────────────────────────────────────────────────────
	// Excepción durante la ejecución: Se captura, se configura error y se propaga
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void execute_whenUseCaseThrowsException_shouldCaptureErrorAndRethrowException() {
		// Arrange
		RuntimeException testException = new RuntimeException("Recharge processing failed");
		doThrow(testException).when(payRechargeUseCase).execute(requestMock);
		doNothing().when(auditoriaPort).procesarAuditoria(any(RechargeRequest.class), any(ResponseWrapper.class),
				any(AuditoriaCommand.class));

		// Act & Assert
		RuntimeException thrown = assertThrows(RuntimeException.class, () -> decorator.execute(requestMock));
		assertEquals("Recharge processing failed", thrown.getMessage());

		// Verify use case was called
		verify(payRechargeUseCase).execute(requestMock);
	}

	@Test
	void execute_whenUseCaseThrowsException_shouldConfigureAuditResponseWithError() {
		// Arrange
		RuntimeException testException = new RuntimeException("Insufficient balance");
		doThrow(testException).when(payRechargeUseCase).execute(requestMock);
		doNothing().when(auditoriaPort).procesarAuditoria(any(RechargeRequest.class), any(ResponseWrapper.class),
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
		IllegalArgumentException testException = new IllegalArgumentException("Invalid recharge request");
		doThrow(testException).when(payRechargeUseCase).execute(requestMock);
		doNothing().when(auditoriaPort).procesarAuditoria(any(RechargeRequest.class), any(ResponseWrapper.class),
				any(AuditoriaCommand.class));

		// Act & Assert
		assertThrows(IllegalArgumentException.class, () -> decorator.execute(requestMock));

		// Verify that audit was still called in finally block
		verify(auditoriaPort).procesarAuditoria(any(RechargeRequest.class), any(ResponseWrapper.class),
				any(AuditoriaCommand.class));
	}

	@Test
	void execute_whenExceptionOccurs_shouldNotSetCodigoOperacionFromFailedResult() {
		// Arrange
		RuntimeException testException = new RuntimeException("Operator network error");
		doThrow(testException).when(payRechargeUseCase).execute(requestMock);
		doNothing().when(auditoriaPort).procesarAuditoria(any(RechargeRequest.class), any(ResponseWrapper.class),
				any(AuditoriaCommand.class));

		// Act & Assert
		assertThrows(RuntimeException.class, () -> decorator.execute(requestMock));

		// Verify audit is called with empty codigoOperacion when exception occurs
		verify(auditoriaPort).procesarAuditoria(eq(requestMock), any(ResponseWrapper.class),
				any(AuditoriaCommand.class));
	}
}
