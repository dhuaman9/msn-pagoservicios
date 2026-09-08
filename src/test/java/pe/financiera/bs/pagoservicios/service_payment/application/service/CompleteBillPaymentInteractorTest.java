package pe.financiera.bs.pagoservicios.service_payment.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.AutorizacionCommand;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.OperationResponse;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.PaymentResultRequest;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.ProductoCommand;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.ProductoResult;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Transaction;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.in.CompleteBillPaymentUseCase;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.in.CompleteBillPaymentUseCase.CompleteBillPaymentCommand;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.OperationPort;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.ProductoPort;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.PublishPaymentResultPort;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.TrxOhPayPort;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class CompleteBillPaymentInteractorTest {

	private CompleteBillPaymentInteractor interactor;

	private TrxOhPayPort trxOhPayPort;
	private OperationPort operationPort;
	private ProductoPort productoPort;
    private PublishPaymentResultPort publishPaymentResultPort;

	private Transaction transactionMock;
	private OperationResponse operationMock;
	private ProductoResult productoResultMock;
	private CompleteBillPaymentCommand successCommand;
	private CompleteBillPaymentCommand failureCommand;

	@BeforeEach
	void setUp() {
		trxOhPayPort = mock(TrxOhPayPort.class);
		operationPort = mock(OperationPort.class);
		productoPort = mock(ProductoPort.class);
		publishPaymentResultPort = mock(PublishPaymentResultPort.class);
		interactor = new CompleteBillPaymentInteractor(trxOhPayPort, operationPort, productoPort, publishPaymentResultPort);

		transactionMock = Transaction.builder().transactionId("TXN-001").operationId("OP-001").status("PENDING")
				.createdUser("user-001").amount(BigDecimal.valueOf(100.00)).build();

		operationMock = OperationResponse.builder().operationId("OP-001").operationNumber("NUM-001")
				.type("SERVICE_PAYMENT").status("PENDING").createdUser("user-001").amount(BigDecimal.valueOf(100.00))
				.build();

		productoResultMock = new ProductoResult(1, "87654321", 1, "ACC-001", "N", "CARD-001", "P", "N", "JOHN DOE",
				"DB", null, "user-001", "user-001", "UID-CLIENT-001", "UID-ACCOUNT-001");

		successCommand = new CompleteBillPaymentCommand("NUM-001", "TXN-001", true, BigDecimal.valueOf(100.00),
				"user-001", "OP-001", null, Map.of());

		failureCommand = new CompleteBillPaymentCommand("NUM-001", "TXN-001", false, BigDecimal.valueOf(100.00),
				"user-001", "OP-001", "ERR-001", Map.of());
	}

	// ─────────────────────────────────────────────────────────────────────────
	// resolveOperation: caminos de retorno temprano
	// ─────────────────────────────────────────────────────────────────────────

	@ParameterizedTest
	@MethodSource("provideNullResolutionScenarios")
	void execute_whenTransactionOrOperationNotFound_shouldNotUpdateAnyStatus(Transaction transaction,
			OperationResponse operation) {

		when(operationPort.getTransactionByOperationNumber(anyString())).thenReturn(transaction);
		if (transaction != null) {
			when(operationPort.getOperationById(anyString())).thenReturn(operation);
		}

		interactor.execute(successCommand);

		verify(operationPort, never()).updateTransactionStatus(anyString(), anyString(), anyString());
		verify(operationPort, never()).updateOperationStatus(anyString(), anyString(), anyString());
		verify(trxOhPayPort, never()).reversar(any(AutorizacionCommand.class));
		verify(publishPaymentResultPort, never()).publishResult(any(PaymentResultRequest.class));
	}

	private static Stream<Arguments> provideNullResolutionScenarios() {

		Transaction nullTransaction = null;

		Transaction validTransaction = Transaction.builder().transactionId("TXN-001").operationId("OP-001")
				.status("PENDING").createdUser("user-001").amount(BigDecimal.valueOf(100.00)).build();

		OperationResponse nullOperation = null;

		return Stream.of(Arguments.of(nullTransaction, null), Arguments.of(validTransaction, nullOperation));
	}

	@Test
	void execute_whenUserIdNullInCommandAndNullInOperation_shouldNotUpdateAnyStatus() {
		CompleteBillPaymentCommand commandWithNullUser = new CompleteBillPaymentCommand("NUM-001", "TXN-001", true,
				BigDecimal.valueOf(100.00), null, "OP-001", null, Map.of());

		OperationResponse operationWithNullCreatedUser = OperationResponse.builder().operationId("OP-001")
				.type("SERVICE_PAYMENT").createdUser(null).build();

		when(operationPort.getTransactionByOperationNumber(anyString())).thenReturn(transactionMock);
		when(operationPort.getOperationById(anyString())).thenReturn(operationWithNullCreatedUser);

		interactor.execute(commandWithNullUser);

		verify(operationPort, never()).updateTransactionStatus(anyString(), anyString(), anyString());
		verify(operationPort, never()).updateOperationStatus(anyString(), anyString(), anyString());
		verify(publishPaymentResultPort, never()).publishResult(any(PaymentResultRequest.class));
	}

	// ─────────────────────────────────────────────────────────────────────────
	// handleSuccess: pago exitoso
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void execute_whenSuccessTrue_shouldUpdateTransactionAndOperationToCompleted() {
		when(operationPort.getTransactionByOperationNumber("NUM-001")).thenReturn(transactionMock);
		when(operationPort.getOperationById("OP-001")).thenReturn(operationMock);

		interactor.execute(successCommand);

		verify(operationPort).updateTransactionStatus("TXN-001", "COMPLETED", "user-001");
		verify(operationPort).updateOperationStatus("OP-001", "COMPLETED", "user-001");
		verify(productoPort, never()).buscarProducto(any(ProductoCommand.class));
		verify(trxOhPayPort, never()).reversar(any(AutorizacionCommand.class));
		verify(publishPaymentResultPort).publishResult(any(PaymentResultRequest.class));
	}

	@Test
	void execute_whenSuccessTrueAndUserIdNullInCommand_shouldFallbackToOperationCreatedUser() {
		CompleteBillPaymentCommand commandWithNullUser = new CompleteBillPaymentCommand("NUM-001", "TXN-001", true,
				BigDecimal.valueOf(100.00), null, "OP-001", null, Map.of());

		when(operationPort.getTransactionByOperationNumber("NUM-001")).thenReturn(transactionMock);
		when(operationPort.getOperationById("OP-001")).thenReturn(operationMock);

		interactor.execute(commandWithNullUser);

		// userId debe resolverse desde operation.getCreatedUser() = "user-001"
		verify(operationPort).updateTransactionStatus("TXN-001", "COMPLETED", "user-001");
		verify(operationPort).updateOperationStatus("OP-001", "COMPLETED", "user-001");
		verify(publishPaymentResultPort).publishResult(any(PaymentResultRequest.class));
	}

	// ─────────────────────────────────────────────────────────────────────────
	// handleFailure: pago fallido → reverso + NOT_PROCESSED
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void execute_whenSuccessFalse_shouldReversarAndUpdateToNotProcessed() {
		when(operationPort.getTransactionByOperationNumber("NUM-001")).thenReturn(transactionMock);
		when(operationPort.getOperationById("OP-001")).thenReturn(operationMock);
		when(productoPort.buscarProducto(any(ProductoCommand.class))).thenReturn(productoResultMock);

		interactor.execute(failureCommand);

		verify(productoPort).buscarProducto(any(ProductoCommand.class));
		verify(trxOhPayPort).reversar(any(AutorizacionCommand.class));
		verify(operationPort).updateTransactionStatus("TXN-001", "NOT_PROCESSED", "user-001");
		verify(operationPort).updateOperationStatus("OP-001", "NOT_PROCESSED", "user-001");
		verify(publishPaymentResultPort).publishResult(any(PaymentResultRequest.class));
	}

	@Test
	void execute_whenSuccessFalse_shouldNotUpdateToCompleted() {
		when(operationPort.getTransactionByOperationNumber("NUM-001")).thenReturn(transactionMock);
		when(operationPort.getOperationById("OP-001")).thenReturn(operationMock);
		when(productoPort.buscarProducto(any(ProductoCommand.class))).thenReturn(productoResultMock);

		interactor.execute(failureCommand);

		verify(operationPort, never()).updateTransactionStatus(anyString(),
				org.mockito.ArgumentMatchers.eq("COMPLETED"), anyString());
		verify(operationPort, never()).updateOperationStatus(anyString(), org.mockito.ArgumentMatchers.eq("COMPLETED"),
				anyString());
		verify(publishPaymentResultPort).publishResult(any(PaymentResultRequest.class));
	}

	@Test
	void execute_whenSuccessFalse_shouldCallBuscarProductoWithCorrectCodInterno() {
		when(operationPort.getTransactionByOperationNumber("NUM-001")).thenReturn(transactionMock);
		when(operationPort.getOperationById("OP-001")).thenReturn(operationMock);
		when(productoPort.buscarProducto(any(ProductoCommand.class))).thenReturn(productoResultMock);

		interactor.execute(failureCommand);

		verify(productoPort).buscarProducto(org.mockito.ArgumentMatchers
				.argThat(cmd -> cmd.codInterno() != null && cmd.codInterno().contains("user-001")));
		verify(publishPaymentResultPort).publishResult(any(PaymentResultRequest.class));
	}

	@Test
	void execute_failureOnAlreadyNotProcessedTransaction_skipsReverse() {
		when(operationPort.getTransactionByOperationNumber("op-num-5")).thenReturn(
				Transaction.builder().transactionId("tx-5").operationId("op-5").status("NOT_PROCESSED").build());
		when(operationPort.getOperationById("op-5")).thenReturn(OperationResponse.builder().operationId("op-5")
				.operationNumber("op-num-5").amount(new BigDecimal("10.00")).status("NOT_PROCESSED").type("RECHARGE")
				.destinationName("Claro").build());
		interactor.execute(new CompleteBillPaymentUseCase.CompleteBillPaymentCommand("op-num-5", "tx-5", false,
				new BigDecimal("10.00"), "user-5", "op-5", "ERR-01", Map.of()));
		verifyNoInteractions(trxOhPayPort);
		verify(operationPort, never()).updateTransactionStatus(any(), any(), any());
		verify(operationPort, never()).updateOperationStatus(any(), any(), any());
		verify(publishPaymentResultPort).publishResult(any(PaymentResultRequest.class));

	}

	// ─────────────────────────────────────────────────────────────────────────
	// publishResultEvent: pruebas para verificar la publicación de eventos
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void execute_publishResultEvent_whenSuccessTrue_shouldPublishWithSuccessTrue() {
		when(operationPort.getTransactionByOperationNumber("NUM-001")).thenReturn(transactionMock);
		when(operationPort.getOperationById("OP-001")).thenReturn(operationMock);

		interactor.execute(successCommand);

		verify(publishPaymentResultPort).publishResult(org.mockito.ArgumentMatchers.argThat(request ->
			request.getOperationNumber().equals("NUM-001") &&
			request.getTransactionId().equals("TXN-001") &&
			request.getOperationId().equals("OP-001") &&
			request.getAmount().equals(BigDecimal.valueOf(100.00)) &&
			request.getUserId().equals("user-001") &&
			request.isSuccess() == true &&
			request.getCommandTrigger().equals("SERVICE_PAYMENT")
		));
	}

	@Test
	void execute_publishResultEvent_whenSuccessFalse_shouldPublishWithSuccessFalse() {
		when(operationPort.getTransactionByOperationNumber("NUM-001")).thenReturn(transactionMock);
		when(operationPort.getOperationById("OP-001")).thenReturn(operationMock);
		when(productoPort.buscarProducto(any(ProductoCommand.class))).thenReturn(productoResultMock);

		interactor.execute(failureCommand);

		verify(publishPaymentResultPort).publishResult(org.mockito.ArgumentMatchers.argThat(request ->
			request.getOperationNumber().equals("NUM-001") &&
			request.getTransactionId().equals("TXN-001") &&
			request.getOperationId().equals("OP-001") &&
			request.getAmount().equals(BigDecimal.valueOf(100.00)) &&
			request.getUserId().equals("user-001") &&
			request.isSuccess() == false &&
			request.getCommandTrigger().equals("SERVICE_PAYMENT")
		));
	}

	@Test
	void execute_publishResultEvent_shouldIncludeCustomProperties() {
		Map<String, Object> customProps = Map.of("key1", "value1", "key2", "value2");
		CompleteBillPaymentCommand commandWithCustomProps = new CompleteBillPaymentCommand("NUM-001", "TXN-001", true,
				BigDecimal.valueOf(100.00), "user-001", "OP-001", null, customProps);

		when(operationPort.getTransactionByOperationNumber("NUM-001")).thenReturn(transactionMock);
		when(operationPort.getOperationById("OP-001")).thenReturn(operationMock);

		interactor.execute(commandWithCustomProps);

		verify(publishPaymentResultPort).publishResult(org.mockito.ArgumentMatchers.argThat(request ->
			request.getCustomProperties().equals(customProps)
		));
	}

	@Test
	void execute_publishResultEvent_whenUserIdResolvedFromOperation_shouldPublishWithResolvedUserId() {
		CompleteBillPaymentCommand commandWithNullUser = new CompleteBillPaymentCommand("NUM-001", "TXN-001", true,
				BigDecimal.valueOf(100.00), null, "OP-001", null, Map.of());

		when(operationPort.getTransactionByOperationNumber("NUM-001")).thenReturn(transactionMock);
		when(operationPort.getOperationById("OP-001")).thenReturn(operationMock);

		interactor.execute(commandWithNullUser);

		verify(publishPaymentResultPort).publishResult(org.mockito.ArgumentMatchers.argThat(request ->
			request.getUserId().equals("user-001")
		));
	}
}
