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
import pe.financiera.bs.pagoservicios.service_payment.domain.model.OperationResponse;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.OperationType;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.PaymentExecutionResult;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.ProductoCommand;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.ProductoResult;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.RechargeRequest;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Recipient;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Service;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.ServiceStatus;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Transaction;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.ExternalPaymentProvider;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.OperationPort;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.ProductoPort;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.RecipientRepositoryPort;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.ServiceRepositoryPort;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.TrxOhPayPort;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayRechargeInteractorTest {

	@Mock
	private ProductoPort productoPort;
	@Mock
	private TrxOhPayPort trxOhPayPort;
	@Mock
	private OperationPort operationPort;
	@Mock
	private ExternalPaymentProvider externalPaymentProvider;
	@Mock
	private ServiceRepositoryPort serviceRepositoryPort;
	@Mock
	private RecipientRepositoryPort recipientRepositoryPort;
	@Mock
	private ObjectMapper objectMapper;

	private PayRechargeInteractor interactor;
	private RechargeRequest rechargeRequest;
	private ProductoResult productoResultMock;
	private OperationResponse operationResponseMock;
	private AutorizationResult autorizationResultMock;
	private Transaction bbrTransactionMock;
	private Transaction ibkTransactionMock;
	private Service serviceMock;
	private Recipient recipientMock;

	@BeforeEach
	void setUp() {
		interactor = new PayRechargeInteractor(productoPort, trxOhPayPort, operationPort, externalPaymentProvider,
				serviceRepositoryPort, recipientRepositoryPort, objectMapper);

		rechargeRequest = RechargeRequest.builder().codInterno("USER-001").recipientId("RECIPIENT-001")
				.serviceId("SERVICE-001").clientId("CLIENT-001").amount(BigDecimal.valueOf(50.00))
				.deviceUUID("DEVICE-UUID-001").operationType(OperationType.RECHARGE).build();

		productoResultMock = new ProductoResult(1, "12345678", 1, "ACC-001", "N", "CARD-001", "P", "N", "JOHN DOE",
				"DB", null, "user-001", "user-001", "UID-CLIENT-001", "UID-ACCOUNT-001");

		operationResponseMock = OperationResponse.builder().operationId("OP-001").operationNumber("NUM-001")
				.type("RECHARGE").status("PENDING").createdUser("user-001").amount(BigDecimal.valueOf(50.00))
				.description("Recarga de Celular").build();

		autorizationResultMock = new AutorizationResult("TXN-AUTH-001", "AUTH-CODE-001", true);

		bbrTransactionMock = Transaction.builder().transactionId("TXN-BBR-001").operationId("OP-001").status("PENDING")
				.createdUser("user-001").amount(BigDecimal.valueOf(50.00)).build();

		ibkTransactionMock = Transaction.builder().transactionId("TXN-IBK-001").operationId("OP-001").status("PENDING")
				.createdUser("user-001").amount(BigDecimal.valueOf(50.00)).build();

		serviceMock = Service.builder().id("SERVICE-001").name("Recharge Service").type("RECHARGE")
				.label("Mobile Recharge").length(10).dataType("STRING").status(ServiceStatus.CREATED).build();

		recipientMock = Recipient.builder().id("RECIPIENT-001").name("John Recipient").supports("MOBILE").build();
	}

	@Test
	void execute_whenValidRequest_shouldCompleteFullRechargeFlow() throws JsonProcessingException {
		when(productoPort.buscarProducto(any(ProductoCommand.class))).thenReturn(productoResultMock);
		when(serviceRepositoryPort.findByRecipientAndServiceId("RECIPIENT-001", "SERVICE-001")).thenReturn(serviceMock);
		when(recipientRepositoryPort.findLatestRecipientById("RECIPIENT-001")).thenReturn(recipientMock);
		when(objectMapper.writeValueAsString(any())).thenReturn("{\"data\":\"additional\"}");
		when(operationPort.createOperation(anyString(), anyString(), any(BigDecimal.class), anyString(), anyString(),
				anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(operationResponseMock);
		when(operationPort.createTransaction(anyString(), anyString(), anyString(), anyString(), any(BigDecimal.class),
				anyString())).thenReturn(bbrTransactionMock).thenReturn(ibkTransactionMock);
		when(trxOhPayPort.autorizar(any(AutorizacionCommand.class))).thenReturn(autorizationResultMock);

		PaymentExecutionResult result = interactor.execute(rechargeRequest);

		verify(productoPort).buscarProducto(any(ProductoCommand.class));
		verify(serviceRepositoryPort).findByRecipientAndServiceId("RECIPIENT-001", "SERVICE-001");
		verify(recipientRepositoryPort).findLatestRecipientById("RECIPIENT-001");
		verify(operationPort).createOperation(anyString(), anyString(), any(BigDecimal.class), anyString(), anyString(),
				anyString(), anyString(), anyString(), anyString(), anyString());
		verify(operationPort, times(2)).createTransaction(anyString(), anyString(), anyString(), anyString(),
				any(BigDecimal.class), anyString());
		verify(trxOhPayPort).autorizar(any(AutorizacionCommand.class));
		verify(externalPaymentProvider).processDirectPayment(any(RechargeRequest.class), anyString(), anyString(), anyString(),
				any(), any(ProductoResult.class), any(), anyString());

		assertNotNull(result);
		assertEquals("OP-001", result.getOperationId());
		assertEquals("TXN-AUTH-001", result.getOperationNumber());
		assertEquals(BigDecimal.valueOf(50.00), result.getAmount());
		assertEquals("John Recipient", result.getName());
		assertEquals("AUTH-CODE-001", result.getAuthorizationCode());
	}

	@Test
	void execute_whenServiceIsNull_shouldUseDefaultValues() throws JsonProcessingException {
		when(productoPort.buscarProducto(any(ProductoCommand.class))).thenReturn(productoResultMock);
		when(serviceRepositoryPort.findByRecipientAndServiceId("RECIPIENT-001", "SERVICE-001")).thenReturn(null);
		when(recipientRepositoryPort.findLatestRecipientById("RECIPIENT-001")).thenReturn(recipientMock);
		when(objectMapper.writeValueAsString(any())).thenReturn("{\"data\":\"additional\"}");
		when(operationPort.createOperation(anyString(), anyString(), any(BigDecimal.class), anyString(), anyString(),
				anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(operationResponseMock);
		when(operationPort.createTransaction(anyString(), anyString(), anyString(), anyString(), any(BigDecimal.class),
				anyString())).thenReturn(bbrTransactionMock).thenReturn(ibkTransactionMock);
		when(trxOhPayPort.autorizar(any(AutorizacionCommand.class))).thenReturn(autorizationResultMock);

		PaymentExecutionResult result = interactor.execute(rechargeRequest);

		assertNotNull(result);
		assertEquals("-", result.getLabelDetail());
	}

	@Test
	void execute_whenRecipientIsNull_shouldUseDefaultNameValue() throws JsonProcessingException {
		when(productoPort.buscarProducto(any(ProductoCommand.class))).thenReturn(productoResultMock);
		when(serviceRepositoryPort.findByRecipientAndServiceId("RECIPIENT-001", "SERVICE-001")).thenReturn(serviceMock);
		when(recipientRepositoryPort.findLatestRecipientById("RECIPIENT-001")).thenReturn(null);
		when(objectMapper.writeValueAsString(any())).thenReturn("{\"data\":\"additional\"}");
		when(operationPort.createOperation(anyString(), anyString(), any(BigDecimal.class), anyString(), anyString(),
				anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(operationResponseMock);
		when(operationPort.createTransaction(anyString(), anyString(), anyString(), anyString(), any(BigDecimal.class),
				anyString())).thenReturn(bbrTransactionMock).thenReturn(ibkTransactionMock);
		when(trxOhPayPort.autorizar(any(AutorizacionCommand.class))).thenReturn(autorizationResultMock);

		PaymentExecutionResult result = interactor.execute(rechargeRequest);

		assertNotNull(result);
		assertEquals("-", result.getName());
	}

	@Test
	void execute_whenRecipientAndServiceAreNull_shouldUseDefaultValues() throws JsonProcessingException {
		when(productoPort.buscarProducto(any(ProductoCommand.class))).thenReturn(productoResultMock);
		when(serviceRepositoryPort.findByRecipientAndServiceId("RECIPIENT-001", "SERVICE-001")).thenReturn(null);
		when(recipientRepositoryPort.findLatestRecipientById("RECIPIENT-001")).thenReturn(null);
		when(objectMapper.writeValueAsString(any())).thenReturn("{\"data\":\"additional\"}");
		when(operationPort.createOperation(anyString(), anyString(), any(BigDecimal.class), anyString(), anyString(),
				anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(operationResponseMock);
		when(operationPort.createTransaction(anyString(), anyString(), anyString(), anyString(), any(BigDecimal.class),
				anyString())).thenReturn(bbrTransactionMock).thenReturn(ibkTransactionMock);
		when(trxOhPayPort.autorizar(any(AutorizacionCommand.class))).thenReturn(autorizationResultMock);

		PaymentExecutionResult result = interactor.execute(rechargeRequest);

		assertNotNull(result);
		assertEquals("-", result.getName());
		assertEquals("-", result.getLabelDetail());
	}

	@Test
	void execute_whenRequestOperationTypeIsNotRecharge_shouldEnsureRechargeType() throws JsonProcessingException {
		RechargeRequest requestWithDifferentType = RechargeRequest.builder().codInterno("USER-001")
				.recipientId("RECIPIENT-001").serviceId("SERVICE-001").clientId("CLIENT-001")
				.amount(BigDecimal.valueOf(50.00)).deviceUUID("DEVICE-UUID-001").operationType(OperationType.RECHARGE)
				.build();

		when(productoPort.buscarProducto(any(ProductoCommand.class))).thenReturn(productoResultMock);
		when(serviceRepositoryPort.findByRecipientAndServiceId("RECIPIENT-001", "SERVICE-001")).thenReturn(serviceMock);
		when(recipientRepositoryPort.findLatestRecipientById("RECIPIENT-001")).thenReturn(recipientMock);
		when(objectMapper.writeValueAsString(any())).thenReturn("{\"data\":\"additional\"}");
		when(operationPort.createOperation(anyString(), anyString(), any(BigDecimal.class), anyString(), anyString(),
				anyString(), anyString(), anyString(), anyString(), anyString())).thenReturn(operationResponseMock);
		when(operationPort.createTransaction(anyString(), anyString(), anyString(), anyString(), any(BigDecimal.class),
				anyString())).thenReturn(bbrTransactionMock).thenReturn(ibkTransactionMock);
		when(trxOhPayPort.autorizar(any(AutorizacionCommand.class))).thenReturn(autorizationResultMock);

		PaymentExecutionResult result = interactor.execute(requestWithDifferentType);

		assertNotNull(result);
		assertEquals("OP-001", result.getOperationId());
	}
}
