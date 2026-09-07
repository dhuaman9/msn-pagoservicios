package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.financiera.bs.pagoservicios.service_payment.domain.exception.ExternalServiceException;
import pe.financiera.bs.pagoservicios.service_payment.domain.exception.ModelRestClientException;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.AutorizacionCommand;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.AutorizationResult;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.OperationType;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.TrxOhPayRestClient;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.AutorizationRestRequest;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.AutorizationRestResponse;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.ReversaRestResponse;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrxOhPayAdapterTest {

	private TrxOhPayAdapter trxOhPayAdapter;

	private TrxOhPayRestClient trxOhPayRestClient;

    private AutorizacionCommand autorizacionCommandMock;

	@BeforeEach
	void setUp() {
		trxOhPayRestClient = mock(TrxOhPayRestClient.class);
        Clock clock = Clock.fixed(Instant.parse("2026-08-12T10:00:00Z"), ZoneId.of("UTC"));
		trxOhPayAdapter = new TrxOhPayAdapter(trxOhPayRestClient, clock);

		autorizacionCommandMock = AutorizacionCommand.builder().customerUid("UID-CLI-001").accountUid("UID-ACC-001")
				.operationNumber("OP-001-NUM").amount(BigDecimal.valueOf(100.00)).customerDocumentType("1")
				.customerDocumentNumber("12345678").customerFullName("John Doe").operationType(OperationType.RECHARGE)
				.canal("MOBILE").build();
	}

	// ─────────────────────────────────────────────────────────────────────────
	// autorizar: Éxito
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void autorizar_whenSuccessful_shouldCallTrxOhPayRestClientWithCorrectRequest() throws IOException {
		// Arrange
		AutorizationRestResponse restResponse = createAutorisationRestResponse();
		Response<AutorizationRestResponse> response = Response.success(restResponse);
		Call<AutorizationRestResponse> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(trxOhPayRestClient.autorizar(any(AutorizationRestRequest.class))).thenReturn(callMock);

		// Act
		AutorizationResult result = trxOhPayAdapter.autorizar(autorizacionCommandMock);

		// Assert
		assertNotNull(result);
		verify(trxOhPayRestClient).autorizar(any(AutorizationRestRequest.class));
	}

	@Test
	void autorizar_whenSuccessful_shouldReturnMappedAutorisationResult() throws IOException {
		// Arrange
		AutorizationRestResponse restResponse = createAutorisationRestResponse();
		Response<AutorizationRestResponse> response = Response.success(restResponse);
		Call<AutorizationRestResponse> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(trxOhPayRestClient.autorizar(any(AutorizationRestRequest.class))).thenReturn(callMock);

		// Act
		AutorizationResult result = trxOhPayAdapter.autorizar(autorizacionCommandMock);

		// Assert
		assertNotNull(result);
		assertEquals("AUTH-001", result.authorizationCode());
	}



	@Test
	void autorizar_whenSuccessfulWithRechargeType_shouldBuildRequestWithRechargeData() throws IOException {
		// Arrange
		AutorizationRestResponse restResponse = createAutorisationRestResponse();
		Response<AutorizationRestResponse> response = Response.success(restResponse);
		Call<AutorizationRestResponse> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(trxOhPayRestClient.autorizar(any(AutorizationRestRequest.class))).thenReturn(callMock);

		ArgumentCaptor<AutorizationRestRequest> captor = ArgumentCaptor.forClass(AutorizationRestRequest.class);

		// Act
		trxOhPayAdapter.autorizar(autorizacionCommandMock);

		// Assert
		verify(trxOhPayRestClient).autorizar(captor.capture());
		AutorizationRestRequest capturedRequest = captor.getValue();

		assertEquals("17", capturedRequest.groupingCode());
		assertEquals("Recarga de Celular", capturedRequest.commerceName());
		assertEquals("5", capturedRequest.commerceTerminalId());
	}

	@Test
	void autorizar_whenSuccessfulWithServicePaymentType_shouldBuildRequestWithServiceData() throws IOException {
		// Arrange
		autorizacionCommandMock = AutorizacionCommand.builder().customerUid("UID-CLI-001").accountUid("UID-ACC-001")
				.operationNumber("OP-001-NUM").amount(BigDecimal.valueOf(150.00)).customerDocumentType("1")
				.customerDocumentNumber("12345678").customerFullName("Jane Doe")
				.operationType(OperationType.SERVICE_PAYMENT).canal("WEB").build();

		AutorizationRestResponse restResponse = createAutorisationRestResponse();
		Response<AutorizationRestResponse> response = Response.success(restResponse);
		Call<AutorizationRestResponse> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(trxOhPayRestClient.autorizar(any(AutorizationRestRequest.class))).thenReturn(callMock);

		ArgumentCaptor<AutorizationRestRequest> captor = ArgumentCaptor.forClass(AutorizationRestRequest.class);

		// Act
		trxOhPayAdapter.autorizar(autorizacionCommandMock);

		// Assert
		verify(trxOhPayRestClient).autorizar(captor.capture());
		AutorizationRestRequest capturedRequest = captor.getValue();

		assertEquals("16", capturedRequest.groupingCode());
		assertEquals("Pago de servicio", capturedRequest.commerceName());
		assertEquals("4", capturedRequest.commerceTerminalId());
	}

	// ─────────────────────────────────────────────────────────────────────────
	// autorizar: Respuesta HTTP no exitosa
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void autorizar_whenResponseNotSuccessful_shouldThrowModelRestClientException() throws IOException {
		// Arrange
		Response<AutorizationRestResponse> response = Response.error(400, okBody());
		Call<AutorizationRestResponse> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(trxOhPayRestClient.autorizar(any(AutorizationRestRequest.class))).thenReturn(callMock);

		// Act & Assert
		ModelRestClientException exception = assertThrows(ModelRestClientException.class,
				() -> trxOhPayAdapter.autorizar(autorizacionCommandMock));

		assertNotNull(exception.getMessage());
	}

	@Test
	void autorizar_whenResponseServerError_shouldThrowModelRestClientException() throws IOException {
		// Arrange
		Response<AutorizationRestResponse> response = Response.error(500, okBody());
		Call<AutorizationRestResponse> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(trxOhPayRestClient.autorizar(any(AutorizationRestRequest.class))).thenReturn(callMock);

		// Act & Assert
		assertThrows(ModelRestClientException.class, () -> trxOhPayAdapter.autorizar(autorizacionCommandMock));
	}

	// ─────────────────────────────────────────────────────────────────────────
	// autorizar: Cuerpo nulo
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void autorizar_whenResponseBodyNull_shouldThrowExternalServiceException() throws IOException {
		// Arrange
		Response<AutorizationRestResponse> response = Response.success(null);
		Call<AutorizationRestResponse> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(trxOhPayRestClient.autorizar(any(AutorizationRestRequest.class))).thenReturn(callMock);

		// Act & Assert
		ExternalServiceException exception = assertThrows(ExternalServiceException.class,
				() -> trxOhPayAdapter.autorizar(autorizacionCommandMock));

		assertNotNull(exception.getMessage());
		assertEquals("External BBR error: response body is null", exception.getMessage());
	}

	// ─────────────────────────────────────────────────────────────────────────
	// autorizar: IOException
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void autorizar_whenIOExceptionOccurs_shouldThrowExternalServiceException() throws IOException {
		// Arrange
		Call<AutorizationRestResponse> callMock = mock(Call.class);
		when(callMock.execute()).thenThrow(new IOException("Network error"));
		when(trxOhPayRestClient.autorizar(any(AutorizationRestRequest.class))).thenReturn(callMock);

		// Act & Assert
		ExternalServiceException exception = assertThrows(ExternalServiceException.class,
				() -> trxOhPayAdapter.autorizar(autorizacionCommandMock));

		assertNotNull(exception.getMessage());
		assertTrue(exception.getMessage().contains("External BBR error"));
	}

	@Test
	void autorizar_whenConnectionTimeoutOccurs_shouldThrowExternalServiceException() throws IOException {
		// Arrange
		Call<AutorizationRestResponse> callMock = mock(Call.class);
		when(callMock.execute()).thenThrow(new IOException("Connection timeout"));
		when(trxOhPayRestClient.autorizar(any(AutorizationRestRequest.class))).thenReturn(callMock);

		// Act & Assert
		ExternalServiceException exception = assertThrows(ExternalServiceException.class,
				() -> trxOhPayAdapter.autorizar(autorizacionCommandMock));

		assertNotNull(exception.getMessage());
	}

	// ─────────────────────────────────────────────────────────────────────────
	// reversar: Éxito
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void reversar_whenSuccessful_shouldCallTrxOhPayRestClientReversar() throws IOException {
		// Arrange
		Response<ReversaRestResponse> response = Response.success(null);
		Call<ReversaRestResponse> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(trxOhPayRestClient.reversar(any(AutorizationRestRequest.class))).thenReturn(callMock);

		// Act
		trxOhPayAdapter.reversar(autorizacionCommandMock);

		// Assert
		verify(trxOhPayRestClient).reversar(any(AutorizationRestRequest.class));
	}



	@Test
	void reversar_whenSuccessfulWithRechargeType_shouldBuildRequestWithRechargeData() throws IOException {
		// Arrange
		Response<ReversaRestResponse> response = Response.success(null);
		Call<ReversaRestResponse> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(trxOhPayRestClient.reversar(any(AutorizationRestRequest.class))).thenReturn(callMock);

		ArgumentCaptor<AutorizationRestRequest> captor = ArgumentCaptor.forClass(AutorizationRestRequest.class);

		// Act
		trxOhPayAdapter.reversar(autorizacionCommandMock);

		// Assert
		verify(trxOhPayRestClient).reversar(captor.capture());
		AutorizationRestRequest capturedRequest = captor.getValue();

		assertEquals("17", capturedRequest.groupingCode());
		assertEquals("Recarga de Celular", capturedRequest.commerceName());
	}

	// ─────────────────────────────────────────────────────────────────────────
	// reversar: Respuesta HTTP no exitosa
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void reversar_whenResponseNotSuccessful_shouldThrowModelRestClientException() throws IOException {
		// Arrange
		Response<ReversaRestResponse> response = Response.error(400, okBody());
		Call<ReversaRestResponse> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(trxOhPayRestClient.reversar(any(AutorizationRestRequest.class))).thenReturn(callMock);

		// Act & Assert
		ModelRestClientException exception = assertThrows(ModelRestClientException.class,
				() -> trxOhPayAdapter.reversar(autorizacionCommandMock));

		assertNotNull(exception.getMessage());
	}

	@Test
	void reversar_whenResponseServerError_shouldThrowModelRestClientException() throws IOException {
		// Arrange
		Response<ReversaRestResponse> response = Response.error(500, okBody());
		Call<ReversaRestResponse> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(trxOhPayRestClient.reversar(any(AutorizationRestRequest.class))).thenReturn(callMock);

		// Act & Assert
		assertThrows(ModelRestClientException.class, () -> trxOhPayAdapter.reversar(autorizacionCommandMock));
	}

	// ─────────────────────────────────────────────────────────────────────────
	// reversar: IOException
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void reversar_whenIOExceptionOccurs_shouldThrowExternalServiceException() throws IOException {
		// Arrange
		Call<ReversaRestResponse> callMock = mock(Call.class);
		when(callMock.execute()).thenThrow(new IOException("Network error"));
		when(trxOhPayRestClient.reversar(any(AutorizationRestRequest.class))).thenReturn(callMock);

		// Act & Assert
		ExternalServiceException exception = assertThrows(ExternalServiceException.class,
				() -> trxOhPayAdapter.reversar(autorizacionCommandMock));

		assertNotNull(exception.getMessage());
		assertTrue(exception.getMessage().contains("External BBR error"));
	}

	@Test
	void reversar_whenConnectionTimeoutOccurs_shouldThrowExternalServiceException() throws IOException {
		// Arrange
		Call<ReversaRestResponse> callMock = mock(Call.class);
		when(callMock.execute()).thenThrow(new IOException("Socket timeout"));
		when(trxOhPayRestClient.reversar(any(AutorizationRestRequest.class))).thenReturn(callMock);

		// Act & Assert
		assertThrows(ExternalServiceException.class, () -> trxOhPayAdapter.reversar(autorizacionCommandMock));
	}

	// ─────────────────────────────────────────────────────────────────────────
	// Helper methods
	// ─────────────────────────────────────────────────────────────────────────

	private AutorizationRestResponse createAutorisationRestResponse() {
		return new AutorizationRestResponse("1234", "AUTH-001", true);

	}

	private okhttp3.ResponseBody okBody() {
		return okhttp3.ResponseBody.create("", okhttp3.MediaType.parse("application/json"));
	}

	private static void assertTrue(boolean condition) {
		if (!condition) {
			throw new AssertionError("Expected true but was false");
		}
	}
}
