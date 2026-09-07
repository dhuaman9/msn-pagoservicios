package pe.financiera.bs.pagoservicios.service_payment.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.AutorizacionCommand;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.AutorizationResult;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Bill;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.BillList;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.OperationResponse;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.OperationType;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.PaymentExecutionResult;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.PaymentRequest;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.ProductoCommand;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.ProductoResult;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Recipient;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Service;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.ServiceStatus;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Transaction;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.in.GetBillsUseCase;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.in.GetBillsUseCase.GetBillsCommand;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.ExternalPaymentProvider;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.OperationPort;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.ProductoPort;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.RecipientRepositoryPort;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.ServiceRepositoryPort;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.TrxOhPayPort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayServiceInteractorTest {

    @Mock
    private ProductoPort productoPort;
    @Mock
    private TrxOhPayPort trxOhPayPort;
    @Mock
    private OperationPort operationPort;
    @Mock
    private ExternalPaymentProvider externalPaymentProvider;
    @Mock
    private GetBillsUseCase getBillsUseCase;
    @Mock
    private ServiceRepositoryPort serviceRepositoryPort;
    @Mock
    private RecipientRepositoryPort recipientRepositoryPort;
    @Mock
    private ObjectMapper objectMapper;

    private PayServiceInteractor interactor;
    private PaymentRequest paymentRequest;
    private ProductoResult productoResultMock;
    private Service serviceMock;
    private Recipient recipientMock;
    private Bill billMock;
    private BillList billListMock;
    private OperationResponse operationResponseMock;
    private AutorizationResult autorizationResultMock;
    private Transaction bbrTransactionMock;
    private Transaction ibkTransactionMock;

    @BeforeEach
    void setUp() {
        interactor = new PayServiceInteractor(productoPort, trxOhPayPort, operationPort,
                externalPaymentProvider, getBillsUseCase, serviceRepositoryPort, recipientRepositoryPort,
                objectMapper);

        paymentRequest = PaymentRequest.builder()
                .codInterno("USER-001")
                .recipientId("RECIPIENT-001")
                .serviceId("SERVICE-001")
                .billId("BILL-001")
                .clientId("CLIENT-001")
                .amount(BigDecimal.valueOf(100.00))
                .deviceUUID("DEVICE-UUID-001")
                .operationType(OperationType.SERVICE_PAYMENT)
                .build();

        productoResultMock = new ProductoResult(
                1, "12345678", 1, "ACC-001",
                "N", "CARD-001", "P", "N",
                "JOHN DOE", "DB", null, "user-001",
                "user-001", "UID-CLIENT-001", "UID-ACCOUNT-001"
        );

        serviceMock = Service.builder()
                .id("SERVICE-001")
                .name("Service Name")
                .type("SERVICE_TYPE")
                .label("Service Label")
                .length(10)
                .dataType("STRING")
                .status(ServiceStatus.CREATED)
                .build();

        recipientMock = Recipient.builder()
                .id("RECIPIENT-001")
                .name("John Recipient")
                .supports("MOBILE")
                .build();

        billMock = Bill.builder()
                .id("BILL-001")
                .number("BILL-NUM-001")
                .amount(BigDecimal.valueOf(100.00))
                .amountFormat("S/ 100.00")
                .currency("PEN")
                .dueDate(LocalDate.of(2025, 12, 31))
                .description("Service bill")
                .build();

        List<Bill> billsList = new ArrayList<>();
        billsList.add(billMock);

        BillList.ClientInfo clientInfo = BillList.ClientInfo.builder().id("12345").name("Juan Carlos").build();
        billListMock = BillList.builder()
                .client(clientInfo)
                .bills(billsList)
                .build();

        operationResponseMock = OperationResponse.builder()
                .operationId("OP-001")
                .operationNumber("NUM-001")
                .type("SERVICE_PAYMENT")
                .status("PENDING")
                .createdUser("user-001")
                .amount(BigDecimal.valueOf(100.00))
                .description("Payment service")
                .build();

        autorizationResultMock = new AutorizationResult("TXN-AUTH-001", "AUTH-CODE-001", true);

        bbrTransactionMock = Transaction.builder()
                .transactionId("TXN-BBR-001")
                .operationId("OP-001")
                .status("PENDING")
                .createdUser("user-001")
                .amount(BigDecimal.valueOf(100.00))
                .build();

        ibkTransactionMock = Transaction.builder()
                .transactionId("TXN-IBK-001")
                .operationId("OP-001")
                .status("PENDING")
                .createdUser("user-001")
                .amount(BigDecimal.valueOf(100.00))
                .build();
    }

    @Test
    void execute_whenValidRequest_shouldCompleteFullPaymentFlow() throws JsonProcessingException {
        when(productoPort.buscarProducto(any(ProductoCommand.class))).thenReturn(productoResultMock);
        when(serviceRepositoryPort.findByRecipientAndServiceId("RECIPIENT-001", "SERVICE-001"))
                .thenReturn(serviceMock);
        when(recipientRepositoryPort.findLatestRecipientById("RECIPIENT-001"))
                .thenReturn(recipientMock);
        when(getBillsUseCase.execute(any(GetBillsCommand.class)))
                .thenReturn(billListMock);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"data\":\"additional\"}");
        when(operationPort.createOperation(anyString(), anyString(), any(BigDecimal.class), anyString(),
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(operationResponseMock);
        when(operationPort.createTransaction(anyString(), anyString(), anyString(), anyString(),
                any(BigDecimal.class), anyString()))
                .thenReturn(bbrTransactionMock).thenReturn(ibkTransactionMock);
        when(trxOhPayPort.autorizar(any(AutorizacionCommand.class))).thenReturn(autorizationResultMock);

        PaymentExecutionResult result = interactor.execute(paymentRequest);

        verify(productoPort).buscarProducto(any(ProductoCommand.class));
        verify(serviceRepositoryPort).findByRecipientAndServiceId("RECIPIENT-001", "SERVICE-001");
        verify(recipientRepositoryPort).findLatestRecipientById("RECIPIENT-001");
        verify(getBillsUseCase).execute(any(GetBillsCommand.class));
        verify(operationPort).createOperation(anyString(), anyString(), any(BigDecimal.class), anyString(),
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString());
        verify(operationPort, times(2)).createTransaction(anyString(), anyString(), anyString(), anyString(),
                any(BigDecimal.class), anyString());
        verify(trxOhPayPort).autorizar(any(AutorizacionCommand.class));
        verify(externalPaymentProvider).processPayment(any(PaymentRequest.class), anyString(), anyString(),
                anyString(), any(), any(ProductoResult.class), any(), anyString());
        verify(operationPort, times(2)).updateTransactionStatus(anyString(), anyString(), anyString());

        assertNotNull(result);
        assertEquals("OP-001", result.getOperationId());
        assertEquals("NUM-001", result.getOperationNumber());
        assertEquals(BigDecimal.valueOf(100.00), result.getAmount());
        assertEquals("John Recipient", result.getName());
        assertEquals("AUTH-CODE-001", result.getAuthorizationCode());
        assertEquals("Service Label", result.getLabelDetail());
    }

    @Test
    void execute_whenServiceIsNull_shouldUseDefaultLabelDetail() throws JsonProcessingException {
        when(productoPort.buscarProducto(any(ProductoCommand.class))).thenReturn(productoResultMock);
        when(serviceRepositoryPort.findByRecipientAndServiceId("RECIPIENT-001", "SERVICE-001"))
                .thenReturn(null);
        when(recipientRepositoryPort.findLatestRecipientById("RECIPIENT-001"))
                .thenReturn(recipientMock);
        when(getBillsUseCase.execute(any(GetBillsCommand.class)))
                .thenReturn(billListMock);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"data\":\"additional\"}");
        when(operationPort.createOperation(anyString(), anyString(), any(BigDecimal.class), anyString(),
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(operationResponseMock);
        when(operationPort.createTransaction(anyString(), anyString(), anyString(), anyString(),
                any(BigDecimal.class), anyString()))
                .thenReturn(bbrTransactionMock).thenReturn(ibkTransactionMock);
        when(trxOhPayPort.autorizar(any(AutorizacionCommand.class))).thenReturn(autorizationResultMock);

        PaymentExecutionResult result = interactor.execute(paymentRequest);

        assertNotNull(result);
        assertEquals("-", result.getLabelDetail());
    }

    @Test
    void execute_whenRecipientIsNull_shouldUseDefaultNameValue() throws JsonProcessingException {
        when(productoPort.buscarProducto(any(ProductoCommand.class))).thenReturn(productoResultMock);
        when(serviceRepositoryPort.findByRecipientAndServiceId("RECIPIENT-001", "SERVICE-001"))
                .thenReturn(serviceMock);
        when(recipientRepositoryPort.findLatestRecipientById("RECIPIENT-001"))
                .thenReturn(null);
        when(getBillsUseCase.execute(any(GetBillsCommand.class)))
                .thenReturn(billListMock);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"data\":\"additional\"}");
        when(operationPort.createOperation(anyString(), anyString(), any(BigDecimal.class), anyString(),
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(operationResponseMock);
        when(operationPort.createTransaction(anyString(), anyString(), anyString(), anyString(),
                any(BigDecimal.class), anyString()))
                .thenReturn(bbrTransactionMock).thenReturn(ibkTransactionMock);
        when(trxOhPayPort.autorizar(any(AutorizacionCommand.class))).thenReturn(autorizationResultMock);

        PaymentExecutionResult result = interactor.execute(paymentRequest);

        assertNotNull(result);
        assertEquals("-", result.getName());
    }

    @Test
    void execute_whenBillNotFoundInList_shouldThrowException() {
        PaymentRequest requestWithNonExistentBillId = PaymentRequest.builder()
                .codInterno("USER-001")
                .recipientId("RECIPIENT-001")
                .serviceId("SERVICE-001")
                .billId("BILL-NONEXISTENT")
                .clientId("CLIENT-001")
                .amount(BigDecimal.valueOf(100.00))
                .deviceUUID("DEVICE-UUID-001")
                .operationType(OperationType.SERVICE_PAYMENT)
                .build();

        when(productoPort.buscarProducto(any(ProductoCommand.class))).thenReturn(productoResultMock);
        when(serviceRepositoryPort.findByRecipientAndServiceId("RECIPIENT-001", "SERVICE-001"))
                .thenReturn(serviceMock);
        when(recipientRepositoryPort.findLatestRecipientById("RECIPIENT-001"))
                .thenReturn(recipientMock);
        when(getBillsUseCase.execute(any(GetBillsCommand.class)))
                .thenReturn(billListMock);

        org.junit.jupiter.api.Assertions.assertThrows(Exception.class, () ->
                interactor.execute(requestWithNonExistentBillId)
        );
    }

    @Test
    void execute_whenRecipientAndServiceAreNull_shouldUseDefaultValues() throws JsonProcessingException {
        when(productoPort.buscarProducto(any(ProductoCommand.class))).thenReturn(productoResultMock);
        when(serviceRepositoryPort.findByRecipientAndServiceId("RECIPIENT-001", "SERVICE-001"))
                .thenReturn(null);
        when(recipientRepositoryPort.findLatestRecipientById("RECIPIENT-001"))
                .thenReturn(null);
        when(getBillsUseCase.execute(any(GetBillsCommand.class)))
                .thenReturn(billListMock);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"data\":\"additional\"}");
        when(operationPort.createOperation(anyString(), anyString(), any(BigDecimal.class), anyString(),
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(operationResponseMock);
        when(operationPort.createTransaction(anyString(), anyString(), anyString(), anyString(),
                any(BigDecimal.class), anyString()))
                .thenReturn(bbrTransactionMock).thenReturn(ibkTransactionMock);
        when(trxOhPayPort.autorizar(any(AutorizacionCommand.class))).thenReturn(autorizationResultMock);

        PaymentExecutionResult result = interactor.execute(paymentRequest);

        assertNotNull(result);
        assertEquals("-", result.getName());
        assertEquals("-", result.getLabelDetail());
    }

    @Test
    void execute_whenOperationAmountIsNull_shouldUseBillAmount() throws JsonProcessingException {
        OperationResponse operationWithNullAmount = OperationResponse.builder()
                .operationId("OP-001")
                .operationNumber("NUM-001")
                .type("SERVICE_PAYMENT")
                .status("PENDING")
                .createdUser("user-001")
                .amount(null)
                .description("Payment service")
                .build();

        when(productoPort.buscarProducto(any(ProductoCommand.class))).thenReturn(productoResultMock);
        when(serviceRepositoryPort.findByRecipientAndServiceId("RECIPIENT-001", "SERVICE-001"))
                .thenReturn(serviceMock);
        when(recipientRepositoryPort.findLatestRecipientById("RECIPIENT-001"))
                .thenReturn(recipientMock);
        when(getBillsUseCase.execute(any(GetBillsCommand.class)))
                .thenReturn(billListMock);
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"data\":\"additional\"}");
        when(operationPort.createOperation(anyString(), anyString(), any(BigDecimal.class), anyString(),
                anyString(), anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(operationWithNullAmount);
        when(operationPort.createTransaction(anyString(), anyString(), anyString(), anyString(),
                any(BigDecimal.class), anyString()))
                .thenReturn(bbrTransactionMock).thenReturn(ibkTransactionMock);
        when(trxOhPayPort.autorizar(any(AutorizacionCommand.class))).thenReturn(autorizationResultMock);

        PaymentExecutionResult result = interactor.execute(paymentRequest);

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(100.00), result.getAmount());
    }
}

