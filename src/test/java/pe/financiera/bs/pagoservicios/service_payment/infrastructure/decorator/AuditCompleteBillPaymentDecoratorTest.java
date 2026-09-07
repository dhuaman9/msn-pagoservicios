package pe.financiera.bs.pagoservicios.service_payment.infrastructure.decorator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.AuditoriaCommand;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.in.CompleteBillPaymentUseCase;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.AuditoriaPort;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.ResponseWrapper;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditCompleteBillPaymentDecoratorTest {

	private AuditCompleteBillPaymentDecorator decorator;

	private CompleteBillPaymentUseCase completeBillPaymentUseCase;
	private AuditoriaPort auditoriaPort;

	private CompleteBillPaymentUseCase.CompleteBillPaymentCommand commandMockFalse;
    private CompleteBillPaymentUseCase.CompleteBillPaymentCommand commandMockTrue;

	@BeforeEach
	void setUp() {
		completeBillPaymentUseCase = mock(CompleteBillPaymentUseCase.class);
		auditoriaPort = mock(AuditoriaPort.class);
		decorator = new AuditCompleteBillPaymentDecorator(completeBillPaymentUseCase, auditoriaPort);

		commandMockFalse = new CompleteBillPaymentUseCase.CompleteBillPaymentCommand("1234", "TX-5678", false,
				BigDecimal.valueOf(10000), "USER-123", "OP-001", null, null);

        commandMockTrue = new CompleteBillPaymentUseCase.CompleteBillPaymentCommand("1234", "TX-5678", true,
            BigDecimal.valueOf(10000), "USER-123", "OP-001", "12", null);

	}

	// ─────────────────────────────────────────────────────────────────────────
	// Ejecución exitosa: command.success() == true
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void execute_whenCommandSuccessIsTrue_shouldCallUseCaseAndProcessAuditWithOK() {

		doNothing().when(completeBillPaymentUseCase).execute(commandMockTrue);
		doNothing().when(auditoriaPort).procesarAuditoria(any(), any(ResponseWrapper.class),
				any(AuditoriaCommand.class));

		// Act
		decorator.execute(commandMockTrue);

		// Assert
		verify(completeBillPaymentUseCase).execute(commandMockTrue);
		verify(auditoriaPort).procesarAuditoria(any(), any(ResponseWrapper.class), any(AuditoriaCommand.class));
	}

	@Test
	void execute_whenCommandSuccessIsTrue_shouldAuditWithCorrectCommandData() {
		// Arrange

		doNothing().when(completeBillPaymentUseCase).execute(commandMockTrue);
		doNothing().when(auditoriaPort).procesarAuditoria(any(), any(ResponseWrapper.class),
				any(AuditoriaCommand.class));

		// Act
		decorator.execute(commandMockTrue);

		// Assert
		verify(completeBillPaymentUseCase).execute(eq(commandMockTrue));
		verify(auditoriaPort).procesarAuditoria(any(), any(ResponseWrapper.class), any(AuditoriaCommand.class));
	}

	// ─────────────────────────────────────────────────────────────────────────
	// Ejecución con fallo controlado: command.success() == false
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void execute_whenCommandSuccessIsFalse_shouldConfigureErrorResponseAndProcessAudit() {
		// Arrange

		doNothing().when(completeBillPaymentUseCase).execute(commandMockFalse);
		doNothing().when(auditoriaPort).procesarAuditoria(any(), any(ResponseWrapper.class),
				any(AuditoriaCommand.class));

		// Act
		decorator.execute(commandMockFalse);

		// Assert
		verify(completeBillPaymentUseCase).execute(commandMockFalse);
		verify(auditoriaPort).procesarAuditoria(any(), any(ResponseWrapper.class), any(AuditoriaCommand.class));
	}

	@Test
	void execute_whenCommandSuccessIsFalse_shouldStillProcessAuditInFinallyBlock() {
		// Arrange

		doNothing().when(completeBillPaymentUseCase).execute(commandMockFalse);
		doNothing().when(auditoriaPort).procesarAuditoria(any(), any(ResponseWrapper.class),
				any(AuditoriaCommand.class));

		// Act
		decorator.execute(commandMockFalse);

		// Assert - Verify audit is called even when success is false
		verify(auditoriaPort).procesarAuditoria(any(), any(ResponseWrapper.class), any(AuditoriaCommand.class));
	}

	// ─────────────────────────────────────────────────────────────────────────
	// Excepción no controlada: Verifica que se propague y ejecute auditoría de
	// error
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void execute_whenUseCaseThrowsException_shouldPropagateExceptionAndProcessErrorAudit() {
		// Arrange
		RuntimeException testException = new RuntimeException("Payment processing failed");
		doThrow(testException).when(completeBillPaymentUseCase).execute(commandMockTrue);
		doNothing().when(auditoriaPort).procesarAuditoria(any(), any(ResponseWrapper.class),
				any(AuditoriaCommand.class));

		// Act & Assert
		assertThrows(RuntimeException.class, () -> decorator.execute(commandMockTrue));

		// Verify that audit was still called in the finally block
		verify(auditoriaPort).procesarAuditoria(any(), any(ResponseWrapper.class), any(AuditoriaCommand.class));
	}

	@Test
	void execute_whenUseCaseThrowsException_shouldCatchAndRethrowException() {
		// Arrange
		RuntimeException testException = new RuntimeException("Unauthorized transaction");
		doThrow(testException).when(completeBillPaymentUseCase).execute(commandMockTrue);
		doNothing().when(auditoriaPort).procesarAuditoria(any(), any(ResponseWrapper.class),
				any(AuditoriaCommand.class));

		// Act & Assert
		RuntimeException thrown = assertThrows(RuntimeException.class, () -> decorator.execute(commandMockTrue));
		assert thrown.getMessage().equals("Unauthorized transaction");

		// Verify use case was called once before throwing
		verify(completeBillPaymentUseCase).execute(commandMockTrue);
	}

	@Test
	void execute_whenExceptionOccurs_shouldExecuteAuditEvenIfUseCaseFailsCompletely() {
		// Arrange
		doThrow(new IllegalArgumentException("Invalid operation")).when(completeBillPaymentUseCase)
				.execute(commandMockTrue);
		doNothing().when(auditoriaPort).procesarAuditoria(any(), any(ResponseWrapper.class),
				any(AuditoriaCommand.class));

		// Act & Assert
		assertThrows(IllegalArgumentException.class, () -> decorator.execute(commandMockTrue));

		// Verify audit was called in finally block despite exception
		verify(auditoriaPort).procesarAuditoria(any(), any(ResponseWrapper.class), any(AuditoriaCommand.class));
	}
}
