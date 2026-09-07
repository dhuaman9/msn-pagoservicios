package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.financiera.bs.pagoservicios.service_payment.domain.exception.ExternalServiceException;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Bill;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.BillList;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.V2GatewayInterbankRestClient;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.BillDto;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.BillResponseDto;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.ClientBillDto;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BillProviderAdapterTest {

	private BillProviderAdapter billProviderAdapter;

	private V2GatewayInterbankRestClient interbankRestClient;

	private String recipientId;
	private String serviceId;
	private String clientId;

	@BeforeEach
	void setUp() {
		interbankRestClient = mock(V2GatewayInterbankRestClient.class);
		billProviderAdapter = new BillProviderAdapter(interbankRestClient);

		recipientId = "REC-001";
		serviceId = "SVC-001";
		clientId = "CLI-001";
	}

	// ─────────────────────────────────────────────────────────────────────────
	// Éxito: Llamada exitosa con mapeo correcto de BillList
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void fetchBills_whenSuccessful_shouldCallGatewayWithCorrectParameters() throws IOException {
		// Arrange
		BillResponseDto responseDto = createBillResponseDto();
		Response<BillResponseDto> response = Response.success(responseDto);
		Call<BillResponseDto> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(interbankRestClient.getBills(recipientId, serviceId, clientId)).thenReturn(callMock);

		// Act
		BillList result = billProviderAdapter.fetchBills(recipientId, serviceId, clientId);

		// Assert
		assertNotNull(result);
		verify(interbankRestClient).getBills(recipientId, serviceId, clientId);
	}

	@Test
	void fetchBills_whenSuccessful_shouldMapBillsCorrectly() throws IOException {
		// Arrange
		BillResponseDto responseDto = createBillResponseDto();
		Response<BillResponseDto> response = Response.success(responseDto);
		Call<BillResponseDto> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(interbankRestClient.getBills(recipientId, serviceId, clientId)).thenReturn(callMock);

		// Act
		BillList result = billProviderAdapter.fetchBills(recipientId, serviceId, clientId);

		// Assert
		assertNotNull(result);
		assertNotNull(result.getBills());
		assertEquals(1, result.getBills().size());

		Bill bill = result.getBills().get(0);
		assertEquals("BILL-001", bill.getId());
		assertEquals(new BigDecimal("150.50"), bill.getAmount());
		assertEquals("S/ 150.50", bill.getAmountFormat());
		assertEquals("USD", bill.getCurrency());
	}

	@Test
	void fetchBills_whenSuccessful_shouldMapClientInfoCorrectly() throws IOException {
		// Arrange
		BillResponseDto responseDto = createBillResponseDto();
		Response<BillResponseDto> response = Response.success(responseDto);
		Call<BillResponseDto> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(interbankRestClient.getBills(recipientId, serviceId, clientId)).thenReturn(callMock);

		// Act
		BillList result = billProviderAdapter.fetchBills(recipientId, serviceId, clientId);

		// Assert
		assertNotNull(result);
		assertNotNull(result.getClient());
		assertEquals("CLI-001", result.getClient().getId());
		assertEquals("Client Name", result.getClient().getName());
	}

	@Test
	void fetchBills_whenSuccessful_shouldParseDatesCorrectly() throws IOException {
		// Arrange
		BillResponseDto responseDto = createBillResponseDto();
		Response<BillResponseDto> response = Response.success(responseDto);
		Call<BillResponseDto> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(interbankRestClient.getBills(recipientId, serviceId, clientId)).thenReturn(callMock);

		// Act
		BillList result = billProviderAdapter.fetchBills(recipientId, serviceId, clientId);

		// Assert
		assertNotNull(result.getBills().get(0).getDueDate());
		assertEquals(LocalDate.parse("2026-12-31"), result.getBills().get(0).getDueDate());
	}

	@Test
	void fetchBills_whenSuccessful_shouldConvertAmountsToCorrectFormat() throws IOException {
		// Arrange
		BillResponseDto responseDto = createBillResponseDto();
		Response<BillResponseDto> response = Response.success(responseDto);
		Call<BillResponseDto> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(interbankRestClient.getBills(recipientId, serviceId, clientId)).thenReturn(callMock);

		// Act
		BillList result = billProviderAdapter.fetchBills(recipientId, serviceId, clientId);

		// Assert
		Bill bill = result.getBills().get(0);
		assertEquals(new BigDecimal("150.50"), bill.getAmount());
		assertEquals(new BigDecimal("10.00"), bill.getDiscount());
		assertEquals(new BigDecimal("5.50"), bill.getCommission());
	}

	// ─────────────────────────────────────────────────────────────────────────
	// Respuesta nula: body es null
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void fetchBills_whenResponseBodyIsNull_shouldReturnEmptyBillList() throws IOException {
		// Arrange
		Response<BillResponseDto> response = Response.success(null);
		Call<BillResponseDto> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(interbankRestClient.getBills(recipientId, serviceId, clientId)).thenReturn(callMock);

		// Act
		BillList result = billProviderAdapter.fetchBills(recipientId, serviceId, clientId);

		// Assert
		assertNotNull(result);
		assertNotNull(result.getBills());
		assertTrue(result.getBills().isEmpty());
	}

	@Test
	void fetchBills_whenResponseBodyIsNull_shouldNotThrowException() throws IOException {
		// Arrange
		Response<BillResponseDto> response = Response.success(null);
		Call<BillResponseDto> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(interbankRestClient.getBills(recipientId, serviceId, clientId)).thenReturn(callMock);

		// Act & Assert - Should not throw
		BillList result = billProviderAdapter.fetchBills(recipientId, serviceId, clientId);
		assertNotNull(result);
	}

	// ─────────────────────────────────────────────────────────────────────────
	// Excepción de red: IOException
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void fetchBills_whenIOExceptionOccurs_shouldThrowExternalServiceException() throws IOException {
		// Arrange
		Call<BillResponseDto> callMock = mock(Call.class);
		when(callMock.execute()).thenThrow(new IOException("Network error"));
		when(interbankRestClient.getBills(recipientId, serviceId, clientId)).thenReturn(callMock);

		// Act & Assert
		ExternalServiceException exception = assertThrows(ExternalServiceException.class,
				() -> billProviderAdapter.fetchBills(recipientId, serviceId, clientId));

		assertNotNull(exception.getMessage());
		assertTrue(exception.getMessage().contains("External Interbank error"));
		assertTrue(exception.getMessage().contains("Network error"));
	}

	@Test
	void fetchBills_whenIOExceptionOccurs_shouldWrapExceptionWithCorrectMessage() throws IOException {
		// Arrange
		String errorMessage = "Connection timeout";
		Call<BillResponseDto> callMock = mock(Call.class);
		when(callMock.execute()).thenThrow(new IOException(errorMessage));
		when(interbankRestClient.getBills(recipientId, serviceId, clientId)).thenReturn(callMock);

		// Act & Assert
		ExternalServiceException exception = assertThrows(ExternalServiceException.class,
				() -> billProviderAdapter.fetchBills(recipientId, serviceId, clientId));

		assertEquals("External Interbank error: " + errorMessage, exception.getMessage());
	}

	@Test
	void fetchBills_whenIOExceptionOccurs_shouldNotCatchException() throws IOException {
		// Arrange
		Call<BillResponseDto> callMock = mock(Call.class);
		when(callMock.execute()).thenThrow(new IOException("Server unavailable"));
		when(interbankRestClient.getBills(recipientId, serviceId, clientId)).thenReturn(callMock);

		// Act & Assert - Exception should be thrown, not caught silently
		assertThrows(ExternalServiceException.class,
				() -> billProviderAdapter.fetchBills(recipientId, serviceId, clientId));
	}

	// ─────────────────────────────────────────────────────────────────────────
	// Helper methods to create test data
	// ─────────────────────────────────────────────────────────────────────────

	private BillResponseDto createBillResponseDto() {
		BillDto billDto = BillDto.builder().id("BILL-001").totalAmount("150.50").currency("USD").discount("10.00")
				.commission("5.50").dueDate("2026-12-31").build();

		ClientBillDto clientBillDto = ClientBillDto.builder().id("CLI-001").name("Client Name").build();

		BillResponseDto responseDto = new BillResponseDto();
		responseDto.setBills(List.of(billDto));
		responseDto.setClient(clientBillDto);

		return responseDto;
	}
}
