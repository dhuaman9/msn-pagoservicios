package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.financiera.bs.pagoservicios.service_payment.domain.exception.ExternalServiceException;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Card;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.PaymentRequest;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.ProductoResult;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.V2GatewayInterbankRestClient;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.V2PaymentRequestDto;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExternalPaymentAdapterTest {

	private ExternalPaymentAdapter externalPaymentAdapter;

	private V2GatewayInterbankRestClient interbankRestClient;
	private Clock clock;

	private PaymentRequest paymentRequestMock;
	private ProductoResult productoResultMock;
	private Card cardMock;

	@BeforeEach
	void setUp() {
		interbankRestClient = mock(V2GatewayInterbankRestClient.class);
		clock = Clock.fixed(Instant.parse("2026-08-12T10:00:00Z"), ZoneId.of("UTC"));
		externalPaymentAdapter = new ExternalPaymentAdapter(interbankRestClient, clock);

		paymentRequestMock = PaymentRequest.builder().codInterno("INT-001").recipientId("REC-001").serviceId("SVC-001")
				.billId("BILL-001").clientId("CLI-001").amount(BigDecimal.valueOf(100.00)).deviceUUID("DEVICE-UUID-123")
				.build();

		productoResultMock = new ProductoResult(141, "9990343242", 5721, "4040710909890987", "N", "4040710909890987",
				"P", "N", "JUAN CAERL", "I", null, "TEST", "TEST", "UID1223123", "UID1223123");

		cardMock = Card.builder().token("TOKEN-CARD-123").build();
	}

	// ─────────────────────────────────────────────────────────────────────────
	// Éxito: Construcción correcta de V2PaymentRequestDto y procesamiento exitoso
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void processPayment_whenSuccessful_shouldCallMakePaymentWithCorrectRequestDto() throws IOException {
		// Arrange
		Response<Void> response = Response.success(null);
		Call<Void> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(interbankRestClient.makePayment(any(V2PaymentRequestDto.class))).thenReturn(callMock);

		// Act
		String result = externalPaymentAdapter.processPayment(paymentRequestMock, "OP-001", "OP-001-NUM", "EXT-ACC-123",
				"SOURCE_TYPE", productoResultMock, cardMock, "{\"data\":\"additional\"}");

		// Assert
		assertEquals("SUCCESS", result);
		verify(interbankRestClient).makePayment(any(V2PaymentRequestDto.class));
	}

	@Test
	void processPayment_whenSuccessful_shouldMapPaymentRequestFieldsCorrectly() throws IOException {
		// Arrange
		Response<Void> response = Response.success(null);
		Call<Void> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(interbankRestClient.makePayment(any(V2PaymentRequestDto.class))).thenReturn(callMock);

		ArgumentCaptor<V2PaymentRequestDto> captor = ArgumentCaptor.forClass(V2PaymentRequestDto.class);

		// Act
		externalPaymentAdapter.processPayment(paymentRequestMock, "OP-001", "OP-001-NUM", "EXT-ACC-123", "SOURCE_TYPE",
				productoResultMock, cardMock, "{\"data\":\"additional\"}");

		// Assert
		verify(interbankRestClient).makePayment(captor.capture());
		V2PaymentRequestDto capturedDto = captor.getValue();

		assertEquals("REC-001", capturedDto.getRecipientId());
		assertEquals("SVC-001", capturedDto.getServiceId());
		assertEquals("BILL-001", capturedDto.getBillId());
		assertEquals("CLI-001", capturedDto.getClientId());
		assertEquals("OP-001", capturedDto.getOperationId());
		assertEquals("OP-001-NUM", capturedDto.getOperationNumber());
	}

	@Test
	void processPayment_whenSuccessful_shouldMapProductoResultFieldsCorrectly() throws IOException {
		// Arrange
		Response<Void> response = Response.success(null);
		Call<Void> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(interbankRestClient.makePayment(any(V2PaymentRequestDto.class))).thenReturn(callMock);

		ArgumentCaptor<V2PaymentRequestDto> captor = ArgumentCaptor.forClass(V2PaymentRequestDto.class);

		// Act
		externalPaymentAdapter.processPayment(paymentRequestMock, "OP-001", "OP-001-NUM", "EXT-ACC-123", "SOURCE_TYPE",
				productoResultMock, cardMock, "{\"data\":\"additional\"}");

		// Assert
		verify(interbankRestClient).makePayment(captor.capture());
		V2PaymentRequestDto capturedDto = captor.getValue();

		assertEquals("141", capturedDto.getDocumentType());
		assertEquals("9990343242", capturedDto.getDocumentNumber());
	}

	@Test
	void processPayment_whenSuccessful_shouldMapCardTokenCorrectly() throws IOException {
		// Arrange
		Response<Void> response = Response.success(null);
		Call<Void> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(interbankRestClient.makePayment(any(V2PaymentRequestDto.class))).thenReturn(callMock);

		ArgumentCaptor<V2PaymentRequestDto> captor = ArgumentCaptor.forClass(V2PaymentRequestDto.class);

		// Act
		externalPaymentAdapter.processPayment(paymentRequestMock, "OP-001", "OP-001-NUM", "EXT-ACC-123", "SOURCE_TYPE",
				productoResultMock, cardMock, "{\"data\":\"additional\"}");

		// Assert
		verify(interbankRestClient).makePayment(captor.capture());
		V2PaymentRequestDto capturedDto = captor.getValue();

		assertEquals("TOKEN-CARD-123", capturedDto.getTokenTunki());
	}

	@Test
	void processPayment_whenSuccessful_shouldMapAmountCorrectly() throws IOException {
		// Arrange
		Response<Void> response = Response.success(null);
		Call<Void> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(interbankRestClient.makePayment(any(V2PaymentRequestDto.class))).thenReturn(callMock);

		ArgumentCaptor<V2PaymentRequestDto> captor = ArgumentCaptor.forClass(V2PaymentRequestDto.class);

		// Act
		externalPaymentAdapter.processPayment(paymentRequestMock, "OP-001", "OP-001-NUM", "EXT-ACC-123", "SOURCE_TYPE",
				productoResultMock, cardMock, null);

		// Assert
		verify(interbankRestClient).makePayment(captor.capture());
		V2PaymentRequestDto capturedDto = captor.getValue();

		assertEquals(BigDecimal.valueOf(100.00), capturedDto.getAmount());
		assertEquals("100.0", capturedDto.getTransferAmount());
	}

	@Test
	void processPayment_whenSuccessful_shouldIncludeAdditionalDataInCustomProperties() throws IOException {
		// Arrange
		Response<Void> response = Response.success(null);
		Call<Void> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(interbankRestClient.makePayment(any(V2PaymentRequestDto.class))).thenReturn(callMock);

		ArgumentCaptor<V2PaymentRequestDto> captor = ArgumentCaptor.forClass(V2PaymentRequestDto.class);
		String additionalData = "{\"data\":\"additional\"}";

		// Act
		externalPaymentAdapter.processPayment(paymentRequestMock, "OP-001", "OP-001-NUM", "EXT-ACC-123", "SOURCE_TYPE",
				productoResultMock, cardMock, additionalData);

		// Assert
		verify(interbankRestClient).makePayment(captor.capture());
		V2PaymentRequestDto capturedDto = captor.getValue();

		assertNotNull(capturedDto.getCustomProperties());
		assertEquals(additionalData, capturedDto.getCustomProperties().get("additionalData"));
		assertEquals("DEVICE-UUID-123", capturedDto.getCustomProperties().get("deviceUUID"));
	}

	@Test
	void processPayment_whenSuccessful_shouldReturnSuccessString() throws IOException {
		// Arrange
		Response<Void> response = Response.success(null);
		Call<Void> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(interbankRestClient.makePayment(any(V2PaymentRequestDto.class))).thenReturn(callMock);

		// Act
		String result = externalPaymentAdapter.processPayment(paymentRequestMock, "OP-001", "OP-001-NUM", "EXT-ACC-123",
				"SOURCE_TYPE", productoResultMock, cardMock, null);

		// Assert
		assertEquals("SUCCESS", result);
	}

	@Test
	void processPayment_whenCardIsNull_shouldHandleNullCardToken() throws IOException {
		// Arrange
		Response<Void> response = Response.success(null);
		Call<Void> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(interbankRestClient.makePayment(any(V2PaymentRequestDto.class))).thenReturn(callMock);

		ArgumentCaptor<V2PaymentRequestDto> captor = ArgumentCaptor.forClass(V2PaymentRequestDto.class);

		// Act
		externalPaymentAdapter.processPayment(paymentRequestMock, "OP-001", "OP-001-NUM", "EXT-ACC-123", "SOURCE_TYPE",
				productoResultMock, null, null);

		// Assert
		verify(interbankRestClient).makePayment(captor.capture());
		V2PaymentRequestDto capturedDto = captor.getValue();

		assertEquals(null, capturedDto.getTokenTunki());
	}

	// ─────────────────────────────────────────────────────────────────────────
	// Error de conectividad: IOException
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void processPayment_whenIOExceptionOccurs_shouldThrowExternalServiceException() throws IOException {
		// Arrange
		Call<Void> callMock = mock(Call.class);
		when(callMock.execute()).thenThrow(new IOException("Connection timeout"));
		when(interbankRestClient.makePayment(any(V2PaymentRequestDto.class))).thenReturn(callMock);

		// Act & Assert
		ExternalServiceException exception = assertThrows(ExternalServiceException.class,
				() -> externalPaymentAdapter.processPayment(paymentRequestMock, "OP-001", "OP-001-NUM", "EXT-ACC-123",
						"SOURCE_TYPE", productoResultMock, cardMock, null));

		assertNotNull(exception.getMessage());
		assertTrue(exception.getMessage().contains("External Interbank connectivity error"));
		assertTrue(exception.getMessage().contains("Connection timeout"));
	}

	@Test
	void processPayment_whenIOExceptionOccurs_shouldWrapExceptionWithCorrectMessage() throws IOException {
		// Arrange
		String errorMessage = "Network unreachable";
		Call<Void> callMock = mock(Call.class);
		when(callMock.execute()).thenThrow(new IOException(errorMessage));
		when(interbankRestClient.makePayment(any(V2PaymentRequestDto.class))).thenReturn(callMock);

		// Act & Assert
		ExternalServiceException exception = assertThrows(ExternalServiceException.class,
				() -> externalPaymentAdapter.processPayment(paymentRequestMock, "OP-001", "OP-001-NUM", "EXT-ACC-123",
						"SOURCE_TYPE", productoResultMock, cardMock, null));

		assertEquals("External Interbank connectivity error: " + errorMessage, exception.getMessage());
	}

	@Test
	void processPayment_whenIOExceptionOccurs_shouldNotCatchException() throws IOException {
		// Arrange
		Call<Void> callMock = mock(Call.class);
		when(callMock.execute()).thenThrow(new IOException("Socket timeout"));
		when(interbankRestClient.makePayment(any(V2PaymentRequestDto.class))).thenReturn(callMock);

		// Act & Assert - Exception should be thrown, not caught silently
		assertThrows(ExternalServiceException.class, () -> externalPaymentAdapter.processPayment(paymentRequestMock,
				"OP-001", "OP-001-NUM", "EXT-ACC-123", "SOURCE_TYPE", productoResultMock, cardMock, null));
	}
}
