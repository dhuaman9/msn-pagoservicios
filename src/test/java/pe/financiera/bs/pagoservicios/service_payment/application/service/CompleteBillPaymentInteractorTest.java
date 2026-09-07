package pe.financiera.bs.pagoservicios.service_payment.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.AutorizacionCommand;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.OperationResponse;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.ProductoCommand;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.ProductoResult;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Transaction;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.in.CompleteBillPaymentUseCase.CompleteBillPaymentCommand;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.OperationPort;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.ProductoPort;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.TrxOhPayPort;

import java.math.BigDecimal;
import java.util.Map;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CompleteBillPaymentInteractorTest {

    private CompleteBillPaymentInteractor interactor;

    private TrxOhPayPort trxOhPayPort;
    private OperationPort operationPort;
    private ProductoPort productoPort;

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
        interactor = new CompleteBillPaymentInteractor(trxOhPayPort, operationPort, productoPort);

        transactionMock = Transaction.builder()
                .transactionId("TXN-001")
                .operationId("OP-001")
                .status("PENDING")
                .createdUser("user-001")
                .amount(BigDecimal.valueOf(100.00))
                .build();

        operationMock = OperationResponse.builder()
                .operationId("OP-001")
                .operationNumber("NUM-001")
                .type("SERVICE_PAYMENT")
                .status("PENDING")
                .createdUser("user-001")
                .amount(BigDecimal.valueOf(100.00))
                .build();

        productoResultMock = new ProductoResult(
                1, "87654321", 1, "ACC-001",
                "N", "CARD-001", "P", "N",
                "JOHN DOE", "DB", null, "user-001",
                "user-001", "UID-CLIENT-001", "UID-ACCOUNT-001"
        );

        successCommand = new CompleteBillPaymentCommand(
                "NUM-001", "TXN-001", true, BigDecimal.valueOf(100.00),
                "user-001", "OP-001", null, Map.of()
        );

        failureCommand = new CompleteBillPaymentCommand(
                "NUM-001", "TXN-001", false, BigDecimal.valueOf(100.00),
                "user-001", "OP-001", "ERR-001", Map.of()
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // resolveOperation: caminos de retorno temprano
    // ─────────────────────────────────────────────────────────────────────────

    @ParameterizedTest
    @MethodSource("provideNullResolutionScenarios")
    void execute_whenTransactionOrOperationNotFound_shouldNotUpdateAnyStatus(
            Transaction transaction, OperationResponse operation) {

        when(operationPort.getTransactionByOperationNumber(anyString())).thenReturn(transaction);
        if (transaction != null) {
            when(operationPort.getOperationById(anyString())).thenReturn(operation);
        }

        interactor.execute(successCommand);

        verify(operationPort, never()).updateTransactionStatus(anyString(), anyString(), anyString());
        verify(operationPort, never()).updateOperationStatus(anyString(), anyString(), anyString());
        verify(trxOhPayPort, never()).reversar(any(AutorizacionCommand.class));
    }

    private static Stream<Arguments> provideNullResolutionScenarios() {

        Transaction nullTransaction = null;

        Transaction validTransaction = Transaction.builder()
                .transactionId("TXN-001")
                .operationId("OP-001")
                .status("PENDING")
                .createdUser("user-001")
                .amount(BigDecimal.valueOf(100.00))
                .build();

        OperationResponse nullOperation = null;

        return Stream.of(
                Arguments.of(nullTransaction, null),
                Arguments.of(validTransaction, nullOperation)
        );
    }

    @Test
    void execute_whenUserIdNullInCommandAndNullInOperation_shouldNotUpdateAnyStatus() {
        CompleteBillPaymentCommand commandWithNullUser = new CompleteBillPaymentCommand(
                "NUM-001", "TXN-001", true, BigDecimal.valueOf(100.00),
                null, "OP-001", null, Map.of()
        );

        OperationResponse operationWithNullCreatedUser = OperationResponse.builder()
                .operationId("OP-001")
                .type("SERVICE_PAYMENT")
                .createdUser(null)
                .build();

        when(operationPort.getTransactionByOperationNumber(anyString())).thenReturn(transactionMock);
        when(operationPort.getOperationById(anyString())).thenReturn(operationWithNullCreatedUser);

        interactor.execute(commandWithNullUser);

        verify(operationPort, never()).updateTransactionStatus(anyString(), anyString(), anyString());
        verify(operationPort, never()).updateOperationStatus(anyString(), anyString(), anyString());
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
    }

    @Test
    void execute_whenSuccessTrueAndUserIdNullInCommand_shouldFallbackToOperationCreatedUser() {
        CompleteBillPaymentCommand commandWithNullUser = new CompleteBillPaymentCommand(
                "NUM-001", "TXN-001", true, BigDecimal.valueOf(100.00),
                null, "OP-001", null, Map.of()
        );

        when(operationPort.getTransactionByOperationNumber("NUM-001")).thenReturn(transactionMock);
        when(operationPort.getOperationById("OP-001")).thenReturn(operationMock);

        interactor.execute(commandWithNullUser);

        // userId debe resolverse desde operation.getCreatedUser() = "user-001"
        verify(operationPort).updateTransactionStatus("TXN-001", "COMPLETED", "user-001");
        verify(operationPort).updateOperationStatus("OP-001", "COMPLETED", "user-001");
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
    }

    @Test
    void execute_whenSuccessFalse_shouldNotUpdateToCompleted() {
        when(operationPort.getTransactionByOperationNumber("NUM-001")).thenReturn(transactionMock);
        when(operationPort.getOperationById("OP-001")).thenReturn(operationMock);
        when(productoPort.buscarProducto(any(ProductoCommand.class))).thenReturn(productoResultMock);

        interactor.execute(failureCommand);

        verify(operationPort, never()).updateTransactionStatus(anyString(), org.mockito.ArgumentMatchers.eq("COMPLETED"), anyString());
        verify(operationPort, never()).updateOperationStatus(anyString(), org.mockito.ArgumentMatchers.eq("COMPLETED"), anyString());
    }

    @Test
    void execute_whenSuccessFalse_shouldCallBuscarProductoWithCorrectCodInterno() {
        when(operationPort.getTransactionByOperationNumber("NUM-001")).thenReturn(transactionMock);
        when(operationPort.getOperationById("OP-001")).thenReturn(operationMock);
        when(productoPort.buscarProducto(any(ProductoCommand.class))).thenReturn(productoResultMock);

        interactor.execute(failureCommand);

        verify(productoPort).buscarProducto(org.mockito.ArgumentMatchers.argThat(cmd ->
                cmd.codInterno() != null && cmd.codInterno().contains("user-001")
        ));
    }
}

