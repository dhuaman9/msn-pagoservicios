package pe.financiera.bs.pagoservicios.service_payment.infrastructure.messaging.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.in.CompleteBillPaymentUseCase;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.event.Body;
import pe.financiera.framework.event.base.message.third.party.Error;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.event.TransactionEvent;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BillCompletedV2ConsumerTest {

	private BillCompletedV2Consumer billCompletedV2Consumer;

	private CompleteBillPaymentUseCase completeBillPaymentUseCase;
	private ObjectMapper objectMapper;

	private TransactionEvent transactionEventMock;

	@BeforeEach
	void setUp() {
		completeBillPaymentUseCase = mock(CompleteBillPaymentUseCase.class);
		objectMapper = mock(ObjectMapper.class);
		billCompletedV2Consumer = new BillCompletedV2Consumer(completeBillPaymentUseCase, objectMapper);

		transactionEventMock = createTransactionEvent();
	}

	// ─────────────────────────────────────────────────────────────────────────
	// Éxito al procesar evento completo
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void accept_whenEventProcessedSuccessfully_shouldExtractDataAndExecuteUseCase() throws JsonProcessingException {
		// Arrange
		when(objectMapper.writeValueAsString(transactionEventMock)).thenReturn("{\"event\":\"json\"}");
		doNothing().when(completeBillPaymentUseCase)
				.execute(any(CompleteBillPaymentUseCase.CompleteBillPaymentCommand.class));

		// Act
		billCompletedV2Consumer.accept(transactionEventMock, 0);

		// Assert
		verify(completeBillPaymentUseCase).execute(any(CompleteBillPaymentUseCase.CompleteBillPaymentCommand.class));
	}

	@Test
	void accept_whenEventProcessedSuccessfully_shouldExtractOperationNumberCorrectly() throws JsonProcessingException {
		// Arrange
		when(objectMapper.writeValueAsString(transactionEventMock)).thenReturn("{\"event\":\"json\"}");
		ArgumentCaptor<CompleteBillPaymentUseCase.CompleteBillPaymentCommand> captor = ArgumentCaptor
				.forClass(CompleteBillPaymentUseCase.CompleteBillPaymentCommand.class);
		doNothing().when(completeBillPaymentUseCase).execute(captor.capture());

		// Act
		billCompletedV2Consumer.accept(transactionEventMock, 0);

		// Assert
		CompleteBillPaymentUseCase.CompleteBillPaymentCommand command = captor.getValue();
		assertEquals("OP-001-NUM", command.operationNumber());
	}

	@Test
	void accept_whenEventProcessedSuccessfully_shouldExtractTransactionIdCorrectly() throws JsonProcessingException {
		// Arrange
		when(objectMapper.writeValueAsString(transactionEventMock)).thenReturn("{\"event\":\"json\"}");
		ArgumentCaptor<CompleteBillPaymentUseCase.CompleteBillPaymentCommand> captor = ArgumentCaptor
				.forClass(CompleteBillPaymentUseCase.CompleteBillPaymentCommand.class);
		doNothing().when(completeBillPaymentUseCase).execute(captor.capture());

		// Act
		billCompletedV2Consumer.accept(transactionEventMock, 0);

		// Assert
		CompleteBillPaymentUseCase.CompleteBillPaymentCommand command = captor.getValue();
		assertEquals("TXN-001", command.transactionId());
	}

	@Test
	void accept_whenEventProcessedSuccessfully_shouldExtractResultAsBooleanCorrectly() throws JsonProcessingException {
		// Arrange
		when(objectMapper.writeValueAsString(transactionEventMock)).thenReturn("{\"event\":\"json\"}");
		ArgumentCaptor<CompleteBillPaymentUseCase.CompleteBillPaymentCommand> captor = ArgumentCaptor
				.forClass(CompleteBillPaymentUseCase.CompleteBillPaymentCommand.class);
		doNothing().when(completeBillPaymentUseCase).execute(captor.capture());

		// Act
		billCompletedV2Consumer.accept(transactionEventMock, 0);

		// Assert
		CompleteBillPaymentUseCase.CompleteBillPaymentCommand command = captor.getValue();
		assertTrue(command.success());
	}

	@Test
	void accept_whenEventProcessedSuccessfully_shouldExtractTransferAmountCorrectly() throws JsonProcessingException {
		// Arrange
		when(objectMapper.writeValueAsString(transactionEventMock)).thenReturn("{\"event\":\"json\"}");
		ArgumentCaptor<CompleteBillPaymentUseCase.CompleteBillPaymentCommand> captor = ArgumentCaptor
				.forClass(CompleteBillPaymentUseCase.CompleteBillPaymentCommand.class);
		doNothing().when(completeBillPaymentUseCase).execute(captor.capture());

		// Act
		billCompletedV2Consumer.accept(transactionEventMock, 0);

		// Assert
		CompleteBillPaymentUseCase.CompleteBillPaymentCommand command = captor.getValue();
		assertEquals(BigDecimal.valueOf(100.0), command.amount());
	}

	@Test
	void accept_whenEventProcessedSuccessfully_shouldExtractUserIdCorrectly() throws JsonProcessingException {
		// Arrange
		when(objectMapper.writeValueAsString(transactionEventMock)).thenReturn("{\"event\":\"json\"}");
		ArgumentCaptor<CompleteBillPaymentUseCase.CompleteBillPaymentCommand> captor = ArgumentCaptor
				.forClass(CompleteBillPaymentUseCase.CompleteBillPaymentCommand.class);
		doNothing().when(completeBillPaymentUseCase).execute(captor.capture());

		// Act
		billCompletedV2Consumer.accept(transactionEventMock, 0);

		// Assert
		CompleteBillPaymentUseCase.CompleteBillPaymentCommand command = captor.getValue();
		assertEquals("USER-001", command.userId());
	}

	@Test
	void accept_whenEventProcessedSuccessfully_shouldExtractOperationIdCorrectly() throws JsonProcessingException {
		// Arrange
		when(objectMapper.writeValueAsString(transactionEventMock)).thenReturn("{\"event\":\"json\"}");
		ArgumentCaptor<CompleteBillPaymentUseCase.CompleteBillPaymentCommand> captor = ArgumentCaptor
				.forClass(CompleteBillPaymentUseCase.CompleteBillPaymentCommand.class);
		doNothing().when(completeBillPaymentUseCase).execute(captor.capture());

		// Act
		billCompletedV2Consumer.accept(transactionEventMock, 0);

		// Assert
		CompleteBillPaymentUseCase.CompleteBillPaymentCommand command = captor.getValue();
		assertEquals("OP-001", command.operationId());
	}

	@Test
	void accept_whenEventProcessedSuccessfully_shouldExtractErrorCodeCorrectly() throws JsonProcessingException {
		// Arrange
		Error errorMock = new Error();
		errorMock.setCode("ERR-001");
		transactionEventMock.setError(errorMock);

		when(objectMapper.writeValueAsString(transactionEventMock)).thenReturn("{\"event\":\"json\"}");
		ArgumentCaptor<CompleteBillPaymentUseCase.CompleteBillPaymentCommand> captor = ArgumentCaptor
				.forClass(CompleteBillPaymentUseCase.CompleteBillPaymentCommand.class);
		doNothing().when(completeBillPaymentUseCase).execute(captor.capture());

		// Act
		billCompletedV2Consumer.accept(transactionEventMock, 0);

		// Assert
		CompleteBillPaymentUseCase.CompleteBillPaymentCommand command = captor.getValue();
		assertEquals("ERR-001", command.errorCode());
	}

	@Test
	void accept_whenEventProcessedSuccessfully_shouldSetErrorCodeNullWhenNoError() throws JsonProcessingException {
		// Arrange
		transactionEventMock.setError(null);

		when(objectMapper.writeValueAsString(transactionEventMock)).thenReturn("{\"event\":\"json\"}");
		ArgumentCaptor<CompleteBillPaymentUseCase.CompleteBillPaymentCommand> captor = ArgumentCaptor
				.forClass(CompleteBillPaymentUseCase.CompleteBillPaymentCommand.class);
		doNothing().when(completeBillPaymentUseCase).execute(captor.capture());

		// Act
		billCompletedV2Consumer.accept(transactionEventMock, 0);

		// Assert
		CompleteBillPaymentUseCase.CompleteBillPaymentCommand command = captor.getValue();
		assertNull(command.errorCode());
	}

	@Test
	void accept_whenEventProcessedSuccessfully_shouldExtractCustomPropertiesCorrectly() throws JsonProcessingException {
		// Arrange
		Map<String, Object> customProps = new HashMap<>();
		customProps.put("key1", "value1");
		customProps.put("key2", "value2");
		transactionEventMock.setCustomProperties(customProps);

		when(objectMapper.writeValueAsString(transactionEventMock)).thenReturn("{\"event\":\"json\"}");
		ArgumentCaptor<CompleteBillPaymentUseCase.CompleteBillPaymentCommand> captor = ArgumentCaptor
				.forClass(CompleteBillPaymentUseCase.CompleteBillPaymentCommand.class);
		doNothing().when(completeBillPaymentUseCase).execute(captor.capture());

		// Act
		billCompletedV2Consumer.accept(transactionEventMock, 0);

		// Assert
		CompleteBillPaymentUseCase.CompleteBillPaymentCommand command = captor.getValue();
		assertNotNull(command.customProperties());
		assertEquals("value1", command.customProperties().get("key1"));
		assertEquals("value2", command.customProperties().get("key2"));
	}

	@Test
	void accept_whenEventProcessedSuccessfully_shouldSerializeEventForLogging() throws JsonProcessingException {
		// Arrange
		String expectedJson = "{\"operationNumber\":\"OP-001-NUM\"}";
		when(objectMapper.writeValueAsString(transactionEventMock)).thenReturn(expectedJson);
		doNothing().when(completeBillPaymentUseCase)
				.execute(any(CompleteBillPaymentUseCase.CompleteBillPaymentCommand.class));

		// Act
		billCompletedV2Consumer.accept(transactionEventMock, 0);

		// Assert
		verify(objectMapper).writeValueAsString(transactionEventMock);
	}

	// ─────────────────────────────────────────────────────────────────────────
	// Éxito cuando falla la serialización (JsonProcessingException)
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void accept_whenSerializationFails_shouldCatchExceptionAndContinueExecution() throws JsonProcessingException {
		// Arrange
		when(objectMapper.writeValueAsString(transactionEventMock))
				.thenThrow(new JsonProcessingException("JSON error") {
				});
		doNothing().when(completeBillPaymentUseCase)
				.execute(any(CompleteBillPaymentUseCase.CompleteBillPaymentCommand.class));

		// Act
		billCompletedV2Consumer.accept(transactionEventMock, 0);

		// Assert - Should still execute the use case despite serialization failure
		verify(completeBillPaymentUseCase).execute(any(CompleteBillPaymentUseCase.CompleteBillPaymentCommand.class));
	}

	@Test
	void accept_whenSerializationFails_shouldProcessEventNormally() throws JsonProcessingException {
		// Arrange
		when(objectMapper.writeValueAsString(transactionEventMock))
				.thenThrow(new JsonProcessingException("Serialization failed") {
				});
		ArgumentCaptor<CompleteBillPaymentUseCase.CompleteBillPaymentCommand> captor = ArgumentCaptor
				.forClass(CompleteBillPaymentUseCase.CompleteBillPaymentCommand.class);
		doNothing().when(completeBillPaymentUseCase).execute(captor.capture());

		// Act
		billCompletedV2Consumer.accept(transactionEventMock, 0);

		// Assert
		CompleteBillPaymentUseCase.CompleteBillPaymentCommand command = captor.getValue();
		assertNotNull(command);
		assertEquals("OP-001-NUM", command.operationNumber());
		assertEquals("TXN-001", command.transactionId());
	}

	@Test
	void accept_whenSerializationFails_shouldNotStopEventProcessing() throws JsonProcessingException {
		// Arrange
		when(objectMapper.writeValueAsString(transactionEventMock))
				.thenThrow(new JsonProcessingException("Cannot serialize") {
				});
		doNothing().when(completeBillPaymentUseCase)
				.execute(any(CompleteBillPaymentUseCase.CompleteBillPaymentCommand.class));

		// Act & Assert - Should complete without throwing exception
		billCompletedV2Consumer.accept(transactionEventMock, 0);

		verify(completeBillPaymentUseCase).execute(any(CompleteBillPaymentUseCase.CompleteBillPaymentCommand.class));
	}

	@Test
	void accept_whenResultIsFalse_shouldExtractResultAsfalse() throws JsonProcessingException {
		// Arrange
		Body body = transactionEventMock.getBody();
		body.setResult(false);

		when(objectMapper.writeValueAsString(transactionEventMock)).thenReturn("{\"event\":\"json\"}");
		ArgumentCaptor<CompleteBillPaymentUseCase.CompleteBillPaymentCommand> captor = ArgumentCaptor
				.forClass(CompleteBillPaymentUseCase.CompleteBillPaymentCommand.class);
		doNothing().when(completeBillPaymentUseCase).execute(captor.capture());

		// Act
		billCompletedV2Consumer.accept(transactionEventMock, 0);

		// Assert
		CompleteBillPaymentUseCase.CompleteBillPaymentCommand command = captor.getValue();
		assertFalse(command.success());
	}

	@Test
	void accept_whenResultIsNull_shouldExtractResultAsfalse() throws JsonProcessingException {
		// Arrange
		Body body = transactionEventMock.getBody();
		body.setResult(null);

		when(objectMapper.writeValueAsString(transactionEventMock)).thenReturn("{\"event\":\"json\"}");
		ArgumentCaptor<CompleteBillPaymentUseCase.CompleteBillPaymentCommand> captor = ArgumentCaptor
				.forClass(CompleteBillPaymentUseCase.CompleteBillPaymentCommand.class);
		doNothing().when(completeBillPaymentUseCase).execute(captor.capture());

		// Act
		billCompletedV2Consumer.accept(transactionEventMock, 0);

		// Assert
		CompleteBillPaymentUseCase.CompleteBillPaymentCommand command = captor.getValue();
		assertFalse(command.success());
	}

	// ─────────────────────────────────────────────────────────────────────────
	// Helper methods
	// ─────────────────────────────────────────────────────────────────────────

	private TransactionEvent createTransactionEvent() {
		TransactionEvent event = new TransactionEvent();

		Body body = new Body();
		body.setOperationNumber("OP-001-NUM");
		body.setTransactionId("TXN-001");
		body.setResult(true);
		body.setTransferAmount(BigDecimal.valueOf(100.0));
		body.setUserId("USER-001");
		body.setOperationId("OP-001");

		event.setBody(body);
		event.setError(null);
		event.setCustomProperties(new HashMap<>());

		return event;
	}
}
