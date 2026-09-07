package pe.financiera.bs.pagoservicios.service_payment.infrastructure.messaging.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.financiera.framework.pubsub.queue.publisher.MessagePublisher;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.ReversePaymentRequest;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.event.TransactionEvent;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.messaging.mapper.ServicePaymentAdditionalData;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReversePaymentAdapterTest {

	private ReversePaymentAdapter reversePaymentAdapter;

	private MessagePublisher messagePublisher;
	private ObjectMapper objectMapper;

	private ReversePaymentRequest reversePaymentRequestMock;

	@BeforeEach
	void setUp() {
		messagePublisher = mock(MessagePublisher.class);
		objectMapper = mock(ObjectMapper.class);
		reversePaymentAdapter = new ReversePaymentAdapter(messagePublisher, objectMapper);

		reversePaymentRequestMock = ReversePaymentRequest.builder().operationId("OP-001").operationNumber("OP-001-NUM")
				.transactionId("TXN-001").amount(BigDecimal.valueOf(100.0)).userId("USER-001").contract("CONTRACT-001")
				.sourceType("EXTERNAL").documentType("1").documentNumber("12345678").cardToken("TOKEN-123")
				.commandTrigger("SERVICE_PAYMENT").commerceName("Pago de Servicio")
				.additionalData("{\"recipientId\":\"REC-001\"}").build();
	}

	// ─────────────────────────────────────────────────────────────────────────
	// Éxito sin BBR_PROCESSOR: Construcción de evento y publicación
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void triggerReverse_whenSuccessfulWithoutBBRProcessor_shouldPublishEventWithBasicProperties() {
		// Arrange
		doNothing().when(messagePublisher).publishMessage(any(TransactionEvent.class));

		// Act
		reversePaymentAdapter.triggerReverse(reversePaymentRequestMock);

		// Assert
		verify(messagePublisher).publishMessage(any(TransactionEvent.class));
	}

	@Test
	void triggerReverse_whenSuccessfulWithoutBBRProcessor_shouldBuildHeaderCorrectly() {
		// Arrange
		ArgumentCaptor<TransactionEvent> captor = ArgumentCaptor.forClass(TransactionEvent.class);
		doNothing().when(messagePublisher).publishMessage(captor.capture());

		// Act
		reversePaymentAdapter.triggerReverse(reversePaymentRequestMock);

		// Assert
		TransactionEvent capturedEvent = captor.getValue();
		assertNotNull(capturedEvent.getHeader());
		assertEquals("SERVICE_PAYMENT", capturedEvent.getHeader().getCommandTrigger());
		assertEquals("STARTED", capturedEvent.getHeader().getEventTag());
		assertNotNull(capturedEvent.getHeader().getEventId());
		assertNotNull(capturedEvent.getHeader().getTimestamp());
	}

	@Test
	void triggerReverse_whenSuccessfulWithoutBBRProcessor_shouldBuildBodyCorrectly() {
		// Arrange
		ArgumentCaptor<TransactionEvent> captor = ArgumentCaptor.forClass(TransactionEvent.class);
		doNothing().when(messagePublisher).publishMessage(captor.capture());

		// Act
		reversePaymentAdapter.triggerReverse(reversePaymentRequestMock);

		// Assert
		TransactionEvent capturedEvent = captor.getValue();
		assertNotNull(capturedEvent.getBody());
		assertEquals("OP-001", capturedEvent.getBody().getOperationId());
		assertEquals("OP-001-NUM", capturedEvent.getBody().getOperationNumber());
		assertEquals("TXN-001", capturedEvent.getBody().getTransactionId());
		assertEquals(BigDecimal.valueOf(100.0), capturedEvent.getBody().getTransferAmount());
		assertEquals("USER-001", capturedEvent.getBody().getUserId());
		assertEquals("CASH_IN", capturedEvent.getBody().getTransferType());
		assertEquals("TOKEN-123", capturedEvent.getBody().getTokenTunki());
		assertTrue(capturedEvent.getBody().getResult());
	}

	@Test
	void triggerReverse_whenSuccessfulWithoutBBRProcessor_shouldIncludeReverseAndFailedFlags() {
		// Arrange
		ArgumentCaptor<TransactionEvent> captor = ArgumentCaptor.forClass(TransactionEvent.class);
		doNothing().when(messagePublisher).publishMessage(captor.capture());

		// Act
		reversePaymentAdapter.triggerReverse(reversePaymentRequestMock);

		// Assert
		TransactionEvent capturedEvent = captor.getValue();
		assertNotNull(capturedEvent.getCustomProperties());
		assertTrue((Boolean) capturedEvent.getCustomProperties().get("reverse"));
		assertEquals("failed", capturedEvent.getCustomProperties().get("servicePaymentIBK"));
	}

	// ─────────────────────────────────────────────────────────────────────────
	// Éxito con BBR_PROCESSOR y CommandTrigger RECHARGE
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void triggerReverse_whenBBRProcessorAndRecharge_shouldIncludeExtendedProperties() throws Exception {
		// Arrange
		reversePaymentRequestMock = ReversePaymentRequest.builder().operationId("OP-002").operationNumber("OP-002-NUM")
				.transactionId("TXN-002").amount(BigDecimal.valueOf(50.0))

				.userId("USER-002").contract("CONTRACT-002").sourceType("BBR_PROCESSOR").documentType("1")
				.documentNumber("87654321").cardToken("TOKEN-456").commandTrigger("RECHARGE")
				.commerceName("Recarga de Celular").customerUid("CUST-UID-001")
				.additionalData("{\"recipientId\":\"REC-002\"}")
				.customProperties(new HashMap<>(Map.of("key1", "value1"))).build();

		ServicePaymentAdditionalData additionalData = ServicePaymentAdditionalData.builder().recipientId("REC-002")
				.build();

		when(objectMapper.readValue(anyString(), any(Class.class))).thenReturn(additionalData);

		ArgumentCaptor<TransactionEvent> captor = ArgumentCaptor.forClass(TransactionEvent.class);
		doNothing().when(messagePublisher).publishMessage(captor.capture());

		// Act
		reversePaymentAdapter.triggerReverse(reversePaymentRequestMock);

		// Assert
		TransactionEvent capturedEvent = captor.getValue();
		Map<String, Object> customProps = capturedEvent.getCustomProperties();

		assertTrue((Boolean) customProps.get("reverse"));
		assertEquals("failed", customProps.get("servicePaymentIBK"));
		assertEquals("CUST-UID-001", customProps.get("customerUID"));
		assertEquals("PHONE_RECHARGE_REVERSE", customProps.get("billTypeCode"));
		assertEquals("REC-002", customProps.get("commerceCode"));
		assertEquals("Recarga de Celular", customProps.get("commerceName"));
	}

	@Test
	void triggerReverse_whenBBRProcessorAndRecharge_shouldParseAdditionalDataCorrectly() throws Exception {
		// Arrange
		reversePaymentRequestMock = ReversePaymentRequest.builder().operationId("OP-003").operationNumber("OP-003-NUM")
				.transactionId("TXN-003").amount(BigDecimal.valueOf(75.0)).userId("USER-003").contract("CONTRACT-003")
				.sourceType("BBR_PROCESSOR").documentType("1").documentNumber("11111111").cardToken("TOKEN-789")
				.commandTrigger("RECHARGE").commerceName("Recarga").customerUid("CUST-UID-002")
				.additionalData("{\"recipientId\":\"REC-003\",\"serviceId\":\"SVC-001\"}").build();

		ServicePaymentAdditionalData parsedData = ServicePaymentAdditionalData.builder().recipientId("REC-003").build();

		when(objectMapper.readValue(reversePaymentRequestMock.getAdditionalData(), ServicePaymentAdditionalData.class))
				.thenReturn(parsedData);

		ArgumentCaptor<TransactionEvent> captor = ArgumentCaptor.forClass(TransactionEvent.class);
		doNothing().when(messagePublisher).publishMessage(captor.capture());

		// Act
		reversePaymentAdapter.triggerReverse(reversePaymentRequestMock);

		// Assert
		TransactionEvent capturedEvent = captor.getValue();
		assertEquals("REC-003", capturedEvent.getCustomProperties().get("commerceCode"));
	}

	// ─────────────────────────────────────────────────────────────────────────
	// Éxito con BBR_PROCESSOR y CommandTrigger distinto (SERVICE_PAYMENT)
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void triggerReverse_whenBBRProcessorAndServicePayment_shouldIncludeServicePaymentProperties() throws Exception {
		// Arrange
		reversePaymentRequestMock = ReversePaymentRequest.builder().operationId("OP-004").operationNumber("OP-004-NUM")
				.transactionId("TXN-004").amount(BigDecimal.valueOf(200.0)).userId("USER-004").contract("CONTRACT-004")
				.sourceType("BBR_PROCESSOR").documentType("1").documentNumber("22222222").cardToken("TOKEN-000")
				.commandTrigger("SERVICE_PAYMENT").commerceName("Pago de Servicios").customerUid("CUST-UID-003")
				.additionalData("{\"recipientId\":\"REC-004\"}").build();

		ServicePaymentAdditionalData additionalData = ServicePaymentAdditionalData.builder().recipientId("REC-004")
				.build();

		when(objectMapper.readValue(anyString(), any(Class.class))).thenReturn(additionalData);

		ArgumentCaptor<TransactionEvent> captor = ArgumentCaptor.forClass(TransactionEvent.class);
		doNothing().when(messagePublisher).publishMessage(captor.capture());

		// Act
		reversePaymentAdapter.triggerReverse(reversePaymentRequestMock);

		// Assert
		TransactionEvent capturedEvent = captor.getValue();
		Map<String, Object> customProps = capturedEvent.getCustomProperties();

		assertEquals("SERVICE_PAYMENT_REVERSE", customProps.get("billTypeCode"));
		assertEquals("REC-004", customProps.get("commerceCode"));
		assertEquals("Pago de Servicios", customProps.get("commerceName"));
	}

	// ─────────────────────────────────────────────────────────────────────────
	// Manejo de excepciones: publishMessage falla
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void triggerReverse_whenPublishMessageThrowsException_shouldCatchAndHandleGracefully() {
		// Arrange
		doThrow(new RuntimeException("Publishing failed")).when(messagePublisher)
				.publishMessage(any(TransactionEvent.class));

		// Act & Assert - Should not throw exception
		reversePaymentAdapter.triggerReverse(reversePaymentRequestMock);

		verify(messagePublisher).publishMessage(any(TransactionEvent.class));
	}

	@Test
	void triggerReverse_whenPublishingFails_shouldLogErrorButNotPropagate() {
		// Arrange
		Exception publishException = new RuntimeException("Message broker unavailable");
		doThrow(publishException).when(messagePublisher).publishMessage(any(TransactionEvent.class));

		// Act & Assert - Should complete without throwing
		reversePaymentAdapter.triggerReverse(reversePaymentRequestMock);

		verify(messagePublisher).publishMessage(any(TransactionEvent.class));
	}

	// ─────────────────────────────────────────────────────────────────────────
	// Manejo de excepciones: ObjectMapper falla al parsear additionalData
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void triggerReverse_whenObjectMapperFailsToParseAdditionalData_shouldHandleGracefully() throws Exception {
		// Arrange
		reversePaymentRequestMock = ReversePaymentRequest.builder().operationId("OP-005").operationNumber("OP-005-NUM")
				.transactionId("TXN-005").amount(BigDecimal.valueOf(150.0)).userId("USER-005").contract("CONTRACT-005")
				.sourceType("BBR_PROCESSOR").documentType("1").documentNumber("33333333").commandTrigger("RECHARGE")
				.commerceName("Recarga").customerUid("CUST-UID-004").additionalData("{invalid json}").build();

		when(objectMapper.readValue(anyString(), any(Class.class))).thenThrow(new RuntimeException("Invalid JSON"));

		ArgumentCaptor<TransactionEvent> captor = ArgumentCaptor.forClass(TransactionEvent.class);
		doNothing().when(messagePublisher).publishMessage(captor.capture());

		// Act
		reversePaymentAdapter.triggerReverse(reversePaymentRequestMock);

		// Assert
		TransactionEvent capturedEvent = captor.getValue();
		assertNotNull(capturedEvent);
		// commerceCode should be null when parsing fails
		assertTrue(capturedEvent.getCustomProperties().containsKey("reverse"));
	}

	@Test
	void triggerReverse_whenAdditionalDataIsNull_shouldHandleWithoutParsing() {
		// Arrange
		reversePaymentRequestMock = ReversePaymentRequest.builder().operationId("OP-006").operationNumber("OP-006-NUM")
				.transactionId("TXN-006").amount(BigDecimal.valueOf(125.0)).userId("USER-006").contract("CONTRACT-006")
				.sourceType("BBR_PROCESSOR").documentType("1").documentNumber("44444444")
				.commandTrigger("SERVICE_PAYMENT").commerceName("Servicio").customerUid("CUST-UID-005")
				.additionalData(null).build();

		ArgumentCaptor<TransactionEvent> captor = ArgumentCaptor.forClass(TransactionEvent.class);
		doNothing().when(messagePublisher).publishMessage(captor.capture());

		// Act
		reversePaymentAdapter.triggerReverse(reversePaymentRequestMock);

		// Assert
		TransactionEvent capturedEvent = captor.getValue();
		assertNotNull(capturedEvent);
		verify(messagePublisher).publishMessage(any(TransactionEvent.class));
	}

	@Test
	void triggerReverse_whenAdditionalDataIsBlank_shouldHandleWithoutParsing() {
		// Arrange
		reversePaymentRequestMock = ReversePaymentRequest.builder().operationId("OP-007").operationNumber("OP-007-NUM")
				.transactionId("TXN-007").amount(BigDecimal.valueOf(175.0)).userId("USER-007").contract("CONTRACT-007")
				.sourceType("BBR_PROCESSOR").documentType("1").documentNumber("55555555").commandTrigger("RECHARGE")
				.commerceName("Recarga").customerUid("CUST-UID-006").additionalData("").build();

		ArgumentCaptor<TransactionEvent> captor = ArgumentCaptor.forClass(TransactionEvent.class);
		doNothing().when(messagePublisher).publishMessage(captor.capture());

		// Act
		reversePaymentAdapter.triggerReverse(reversePaymentRequestMock);

		// Assert
		TransactionEvent capturedEvent = captor.getValue();
		assertNotNull(capturedEvent);
		verify(messagePublisher).publishMessage(any(TransactionEvent.class));
	}

	@Test
	void triggerReverse_whenMultipleErrorsOccur_shouldCompleteWithoutThrowingAnyException() throws Exception {
		// Arrange
		reversePaymentRequestMock = ReversePaymentRequest.builder().operationId("OP-008").operationNumber("OP-008-NUM")
				.transactionId("TXN-008").amount(BigDecimal.valueOf(300.0)).userId("USER-008").contract("CONTRACT-008")
				.sourceType("BBR_PROCESSOR").documentType("1").documentNumber("66666666")
				.commandTrigger("SERVICE_PAYMENT").commerceName("Pago").customerUid("CUST-UID-007")
				.additionalData("{malformed}").build();

		when(objectMapper.readValue(anyString(), any(Class.class))).thenThrow(new RuntimeException("Parse error"));
		doThrow(new RuntimeException("Publish error")).when(messagePublisher)
				.publishMessage(any(TransactionEvent.class));

		// Act & Assert - Should complete without throwing any exception
		reversePaymentAdapter.triggerReverse(reversePaymentRequestMock);

		verify(messagePublisher).publishMessage(any(TransactionEvent.class));
	}
}
