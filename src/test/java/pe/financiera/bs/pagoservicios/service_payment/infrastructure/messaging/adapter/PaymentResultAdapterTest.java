package pe.financiera.bs.pagoservicios.service_payment.infrastructure.messaging.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.financiera.framework.pubsub.queue.publisher.MessagePublisher;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.PaymentResultRequest;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.event.Body;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.event.TransactionEvent;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.messaging.mapper.ServicePaymentAdditionalData;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentResultAdapterTest {

	@Mock
	private MessagePublisher incorporateStartedTopic;

	private ObjectMapper objectMapper;

	private PaymentResultAdapter adapter;

	private PaymentResultRequest successRequest;
	private PaymentResultRequest failureRequest;
	private PaymentResultRequest bbrProcessorRequest;

	@BeforeEach
	void setUp() {
		objectMapper = new ObjectMapper();
		adapter = new PaymentResultAdapter(incorporateStartedTopic, objectMapper);

		successRequest = PaymentResultRequest.builder()
				.operationId("OP-001")
				.operationNumber("NUM-001")
				.transactionId("TXN-001")
				.userId("user-001")
				.amount(BigDecimal.valueOf(100.00))
				.commandTrigger("SERVICE_PAYMENT")
				.contract("CONTRACT-001")
				.sourceType("MOBILE_APP")
				.customerUid("UID-001")
				.documentType("DNI")
				.documentNumber("12345678")
				.commerceName("Test Commerce")
				.additionalData(null)
				.success(true)
				.customProperties(Map.of("key1", "value1"))
				.build();

		failureRequest = PaymentResultRequest.builder()
				.operationId("OP-002")
				.operationNumber("NUM-002")
				.transactionId("TXN-002")
				.userId("user-002")
				.amount(BigDecimal.valueOf(50.00))
				.commandTrigger("PHONE_RECHARGE")
				.contract("CONTRACT-002")
				.sourceType("WEB")
				.customerUid("UID-002")
				.documentType("RUC")
				.documentNumber("87654321")
				.commerceName("Claro")
				.additionalData(null)
				.success(false)
				.customProperties(null)
				.build();

		bbrProcessorRequest = PaymentResultRequest.builder()
				.operationId("OP-003")
				.operationNumber("NUM-003")
				.transactionId("TXN-003")
				.userId("user-003")
				.amount(BigDecimal.valueOf(150.00))
				.commandTrigger("RECHARGE")
				.contract("CONTRACT-003")
				.sourceType("BBR_PROCESSOR")
				.customerUid("UID-003")
				.documentType("DNI")
				.documentNumber("98765432")
				.commerceName("Bitel")
				.additionalData("{\"recipientId\":\"RECIPIENT-001\"}")
				.success(true)
				.customProperties(Map.of())
				.build();
	}

	// ─────────────────────────────────────────────────────────────────────────
	// publishResult: casos de éxito
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void publishResult_whenSuccessTrueWithoutBBRProcessor_shouldPublishEventWithCorrectHeader() {
		adapter.publishResult(successRequest);

		ArgumentCaptor<TransactionEvent> captor = ArgumentCaptor.forClass(TransactionEvent.class);
		verify(incorporateStartedTopic).publishMessage(captor.capture());

		TransactionEvent event = captor.getValue();
		assertThat(event.getHeader()).isNotNull();
		assertThat(event.getHeader().getCommandTrigger()).isEqualTo("SERVICE_PAYMENT");
		assertThat(event.getHeader().getEventTag()).isEqualTo("COMPLETED");
		assertThat(event.getHeader().getEventId()).isNotNull();
		assertThat(event.getHeader().getTimestamp()).isNotNull();
	}

	@Test
	void publishResult_whenSuccessTrueWithoutBBRProcessor_shouldPublishEventWithCorrectBody() {
		adapter.publishResult(successRequest);

		ArgumentCaptor<TransactionEvent> captor = ArgumentCaptor.forClass(TransactionEvent.class);
		verify(incorporateStartedTopic).publishMessage(captor.capture());

		Body body = captor.getValue().getBody();
		assertThat(body).isNotNull();
		assertThat(body.getOperationId()).isEqualTo("OP-001");
		assertThat(body.getOperationNumber()).isEqualTo("NUM-001");
		assertThat(body.getTransactionId()).isEqualTo("TXN-001");
		assertThat(body.getTransferAmount()).isEqualTo(BigDecimal.valueOf(100.00));
		assertThat(body.getUserId()).isEqualTo("user-001");
		assertThat(body.getTransferType()).isEqualTo("CASH_IN");
		assertThat(body.getContract()).isEqualTo("CONTRACT-001");
		assertThat(body.getSourceType()).isEqualTo("MOBILE_APP");
		assertThat(body.getDocumentType()).isEqualTo("DNI");
		assertThat(body.getDocumentNumber()).isEqualTo("12345678");
		assertThat(body.getResult()).isTrue();
	}

	@Test
	void publishResult_whenSuccessTrueWithoutBBRProcessor_shouldIncludeCustomProperties() {
		adapter.publishResult(successRequest);

		ArgumentCaptor<TransactionEvent> captor = ArgumentCaptor.forClass(TransactionEvent.class);
		verify(incorporateStartedTopic).publishMessage(captor.capture());

		Map<String, Object> customProps = captor.getValue().getCustomProperties();
		assertThat(customProps).isNotNull();
		assertThat(customProps).containsEntry("servicePaymentIBK", "success");
		assertThat(customProps).containsEntry("key1", "value1");
	}

	// ─────────────────────────────────────────────────────────────────────────
	// publishResult: BBR_PROCESSOR - propiedades dinámicas
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void publishResult_whenSuccessTrueWithBBRProcessor_shouldIncludeDynamicProperties() {
		adapter.publishResult(bbrProcessorRequest);

		ArgumentCaptor<TransactionEvent> captor = ArgumentCaptor.forClass(TransactionEvent.class);
		verify(incorporateStartedTopic).publishMessage(captor.capture());

		Map<String, Object> customProps = captor.getValue().getCustomProperties();
		assertThat(customProps).isNotNull();
		assertThat(customProps).containsEntry("servicePaymentIBK", "success");
		assertThat(customProps).containsEntry("customerUID", "UID-003");
		assertThat(customProps).containsEntry("billTypeCode", "PHONE_RECHARGE");
		assertThat(customProps).containsEntry("commerceCode", "RECIPIENT-001");
		assertThat(customProps).containsEntry("commerceName", "Bitel");
		assertThat(customProps).containsKey("commerceTerminalId");
		assertThat(customProps.get("commerceTerminalId")).isNotNull();
		assertThat(customProps).containsKey("reason");
		assertThat(customProps.get("reason")).isNotNull();
		assertThat(customProps).containsKey("commerceTrxDescription");
		assertThat(customProps.get("commerceTrxDescription")).isNotNull();

	}

	@Test
	void publishResult_whenSuccessTrueWithBBRProcessor_shouldPublishWithSuccessTag() {
		adapter.publishResult(bbrProcessorRequest);

		ArgumentCaptor<TransactionEvent> captor = ArgumentCaptor.forClass(TransactionEvent.class);
		verify(incorporateStartedTopic).publishMessage(captor.capture());

		assertThat(captor.getValue().getHeader().getEventTag()).isEqualTo("COMPLETED");
	}

	// ─────────────────────────────────────────────────────────────────────────
	// publishResult: casos de fallo
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void publishResult_whenSuccessFalse_shouldPublishEventWithFailedTag() {
		adapter.publishResult(failureRequest);

		ArgumentCaptor<TransactionEvent> captor = ArgumentCaptor.forClass(TransactionEvent.class);
		verify(incorporateStartedTopic).publishMessage(captor.capture());

		TransactionEvent event = captor.getValue();
		assertThat(event.getHeader().getEventTag()).isEqualTo("FAILED");
		assertThat(event.getBody().getResult()).isFalse();
	}

	@Test
	void publishResult_whenSuccessFalse_shouldIncludeFailureIndicator() {
		adapter.publishResult(failureRequest);

		ArgumentCaptor<TransactionEvent> captor = ArgumentCaptor.forClass(TransactionEvent.class);
		verify(incorporateStartedTopic).publishMessage(captor.capture());

		Map<String, Object> customProps = captor.getValue().getCustomProperties();
		assertThat(customProps).containsEntry("servicePaymentIBK", "failed");
	}

	@Test
	void publishResult_whenSuccessFalseWithNullCustomProperties_shouldHandleGracefully() {
		adapter.publishResult(failureRequest);

		ArgumentCaptor<TransactionEvent> captor = ArgumentCaptor.forClass(TransactionEvent.class);
		verify(incorporateStartedTopic).publishMessage(captor.capture());

		Map<String, Object> customProps = captor.getValue().getCustomProperties();
		assertThat(customProps).isNotNull();
		assertThat(customProps).containsEntry("servicePaymentIBK", "failed");
	}

	// ─────────────────────────────────────────────────────────────────────────
	// Manejo de excepciones
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void publishResult_whenMessagePublisherThrowsException_shouldCatchAndLog() {
		doThrow(new RuntimeException("Publishing failed")).when(incorporateStartedTopic).publishMessage(any(TransactionEvent.class));

		assertThatCode(() -> adapter.publishResult(successRequest))
				.doesNotThrowAnyException();

		verify(incorporateStartedTopic).publishMessage(any(TransactionEvent.class));
	}

	@Test
	void publishResult_whenMessagePublisherThrowsException_shouldNotPropagateException() {
		Exception publishException = new RuntimeException("Topic unavailable");
		doThrow(publishException).when(incorporateStartedTopic).publishMessage(any(TransactionEvent.class));

		assertThatCode(() -> adapter.publishResult(successRequest))
				.doesNotThrowAnyException();
	}

	@Test
	void publishResult_whenObjectMapperThrowsException_shouldResolveRecipientIdAsNull() {
		PaymentResultRequest requestWithInvalidData = PaymentResultRequest.builder()
				.operationId("OP-004")
				.operationNumber("NUM-004")
				.transactionId("TXN-004")
				.userId("user-004")
				.amount(BigDecimal.valueOf(75.00))
				.commandTrigger("RECHARGE")
				.contract("CONTRACT-004")
				.sourceType("BBR_PROCESSOR")
				.customerUid("UID-004")
				.documentType("DNI")
				.documentNumber("11111111")
				.commerceName("Entel")
				.additionalData("invalid json {]")
				.success(true)
				.customProperties(new HashMap<>())
				.build();

		adapter.publishResult(requestWithInvalidData);

		ArgumentCaptor<TransactionEvent> captor = ArgumentCaptor.forClass(TransactionEvent.class);
		verify(incorporateStartedTopic).publishMessage(captor.capture());

		Map<String, Object> customProps = captor.getValue().getCustomProperties();
		assertThat(customProps).containsEntry("commerceCode", null);
	}

	@Test
	void publishResult_whenAdditionalDataIsNull_shouldNotCallObjectMapper() {
		adapter.publishResult(successRequest);

		verify(incorporateStartedTopic).publishMessage(any(TransactionEvent.class));
		// objectMapper.readValue should not be called when additionalData is null
	}

	@Test
	void publishResult_whenAdditionalDataIsBlank_shouldSetCommerceCodeAsNull() {
		PaymentResultRequest requestWithBlankData = PaymentResultRequest.builder()
				.operationId("OP-005")
				.operationNumber("NUM-005")
				.transactionId("TXN-005")
				.userId("user-005")
				.amount(BigDecimal.valueOf(100.00))
				.commandTrigger("SERVICE_PAYMENT")
				.contract("CONTRACT-005")
				.sourceType("BBR_PROCESSOR")
				.customerUid("UID-005")
				.documentType("DNI")
				.documentNumber("55555555")
				.commerceName("PPL")
				.additionalData("   ")
				.success(true)
				.customProperties(new HashMap<>())
				.build();

		adapter.publishResult(requestWithBlankData);

		ArgumentCaptor<TransactionEvent> captor = ArgumentCaptor.forClass(TransactionEvent.class);
		verify(incorporateStartedTopic).publishMessage(captor.capture());

		Map<String, Object> customProps = captor.getValue().getCustomProperties();
		assertThat(customProps).containsEntry("commerceCode", null);
	}

}
