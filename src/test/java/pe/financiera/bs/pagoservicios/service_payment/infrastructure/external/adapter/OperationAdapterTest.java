package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.financiera.bs.pagoservicios.service_payment.domain.exception.ExternalServiceException;
import pe.financiera.bs.pagoservicios.service_payment.domain.exception.ModelRestClientException;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.OperationResponse;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Transaction;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.V2OperationRestClient;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.V2OperationCreateRequestDto;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.V2OperationResponseDto;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.V2OperationUpdateRequestDto;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.V2ResultTransactionResponseDto;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.V2TransactionCreateRequestDto;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.V2TransactionDto;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.V2TransactionUpdateDto;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperationAdapterTest {

	private OperationAdapter operationAdapter;

	private V2OperationRestClient operationRestClient;

	private String operationNumber;
	private String operationId;
	private String transactionId;
	private String userId;
	private String accountId;

	@BeforeEach
	void setUp() {
		operationRestClient = mock(V2OperationRestClient.class);
		operationAdapter = new OperationAdapter(operationRestClient);

		operationNumber = "OP-001-NUM";
		operationId = "OP-001";
		transactionId = "TXN-001";
		userId = "USER-001";
		accountId = "ACC-001";
	}

	// ─────────────────────────────────────────────────────────────────────────
	// getTransactionByOperationNumber: Éxito
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void getTransactionByOperationNumber_whenSuccessful_shouldReturnMappedTransaction() throws IOException {
		// Arrange
		V2TransactionDto transactionDto = createV2TransactionDto();
		V2ResultTransactionResponseDto resultDto = new V2ResultTransactionResponseDto();
		resultDto.setResult(List.of(transactionDto));

		Response<V2ResultTransactionResponseDto> response = Response.success(resultDto);
		Call<V2ResultTransactionResponseDto> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(operationRestClient.searchTransactionByFilter(anyString())).thenReturn(callMock);

		// Act
		Transaction result = operationAdapter.getTransactionByOperationNumber(operationNumber);

		// Assert
		assertNotNull(result);
		assertEquals("TXN-001", result.getTransactionId());
		assertEquals("OP-001", result.getOperationId());
		assertEquals("COMPLETED", result.getStatus());
		assertEquals(BigDecimal.valueOf(100.00), result.getAmount());
	}

	// ─────────────────────────────────────────────────────────────────────────
	// getTransactionByOperationNumber: Respuesta no exitosa o vacía
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void getTransactionByOperationNumber_whenResponseNotSuccessful_shouldReturnNull() throws IOException {
		// Arrange
		Response<V2ResultTransactionResponseDto> response = Response.error(404, okBody());
		Call<V2ResultTransactionResponseDto> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(operationRestClient.searchTransactionByFilter(anyString())).thenReturn(callMock);

		// Act
		Transaction result = operationAdapter.getTransactionByOperationNumber(operationNumber);

		// Assert
		assertNull(result);
	}

	@Test
	void getTransactionByOperationNumber_whenResponseBodyNull_shouldReturnNull() throws IOException {
		// Arrange
		Response<V2ResultTransactionResponseDto> response = Response.success(null);
		Call<V2ResultTransactionResponseDto> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(operationRestClient.searchTransactionByFilter(anyString())).thenReturn(callMock);

		// Act
		Transaction result = operationAdapter.getTransactionByOperationNumber(operationNumber);

		// Assert
		assertNull(result);
	}

	@Test
	void getTransactionByOperationNumber_whenResultEmpty_shouldReturnNull() throws IOException {
		// Arrange
		V2ResultTransactionResponseDto resultDto = new V2ResultTransactionResponseDto();
		resultDto.setResult(List.of());

		Response<V2ResultTransactionResponseDto> response = Response.success(resultDto);
		Call<V2ResultTransactionResponseDto> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(operationRestClient.searchTransactionByFilter(anyString())).thenReturn(callMock);

		// Act
		Transaction result = operationAdapter.getTransactionByOperationNumber(operationNumber);

		// Assert
		assertNull(result);
	}

	@Test
	void getTransactionByOperationNumber_whenIOExceptionOccurs_shouldThrowExternalServiceException()
			throws IOException {
		// Arrange
		Call<V2ResultTransactionResponseDto> callMock = mock(Call.class);
		when(callMock.execute()).thenThrow(new IOException("Network error"));
		when(operationRestClient.searchTransactionByFilter(anyString())).thenReturn(callMock);

		// Act & Assert
		ExternalServiceException exception = assertThrows(ExternalServiceException.class,
				() -> operationAdapter.getTransactionByOperationNumber(operationNumber));

		assertNotNull(exception.getMessage());
	}

	// ─────────────────────────────────────────────────────────────────────────
	// getOperationById: Éxito
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void getOperationById_whenSuccessful_shouldReturnMappedOperation() throws IOException {
		// Arrange
		V2OperationResponseDto operationDto = createV2OperationResponseDto();
		Response<V2OperationResponseDto> response = Response.success(operationDto);
		Call<V2OperationResponseDto> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(operationRestClient.getOperation(operationId)).thenReturn(callMock);

		// Act
		OperationResponse result = operationAdapter.getOperationById(operationId);

		// Assert
		assertNotNull(result);
		assertEquals("OP-001", result.getOperationId());
		assertEquals("OP-001-NUM", result.getOperationNumber());
		assertEquals(BigDecimal.valueOf(150.00), result.getAmount());
	}

	// ─────────────────────────────────────────────────────────────────────────
	// getOperationById: Respuesta nula o no exitosa
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void getOperationById_whenResponseNotSuccessful_shouldReturnNull() throws IOException {
		// Arrange
		Response<V2OperationResponseDto> response = Response.error(404, okBody());
		Call<V2OperationResponseDto> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(operationRestClient.getOperation(operationId)).thenReturn(callMock);

		// Act
		OperationResponse result = operationAdapter.getOperationById(operationId);

		// Assert
		assertNull(result);
	}

	@Test
	void getOperationById_whenResponseBodyNull_shouldReturnNull() throws IOException {
		// Arrange
		Response<V2OperationResponseDto> response = Response.success(null);
		Call<V2OperationResponseDto> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(operationRestClient.getOperation(operationId)).thenReturn(callMock);

		// Act
		OperationResponse result = operationAdapter.getOperationById(operationId);

		// Assert
		assertNull(result);
	}

	@Test
	void getOperationById_whenIOExceptionOccurs_shouldThrowExternalServiceException() throws IOException {
		// Arrange
		Call<V2OperationResponseDto> callMock = mock(Call.class);
		when(callMock.execute()).thenThrow(new IOException("Connection timeout"));
		when(operationRestClient.getOperation(operationId)).thenReturn(callMock);

		// Act & Assert
		ExternalServiceException exception = assertThrows(ExternalServiceException.class,
				() -> operationAdapter.getOperationById(operationId));

		assertNotNull(exception.getMessage());
	}

	// ─────────────────────────────────────────────────────────────────────────
	// updateTransactionStatus: Éxito y error
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void updateTransactionStatus_whenSuccessful_shouldCallUpdateTransaction() throws IOException {
		// Arrange
		Response<Void> response = Response.success(null);
		Call callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(operationRestClient.updateTransaction(anyString(), any(V2TransactionUpdateDto.class)))
				.thenReturn(callMock);

		// Act
		operationAdapter.updateTransactionStatus(transactionId, "COMPLETED", userId);

		// Assert
		verify(operationRestClient).updateTransaction(anyString(), any(V2TransactionUpdateDto.class));
	}

	@Test
	void updateTransactionStatus_whenIOExceptionOccurs_shouldCatchAndNotThrow() throws IOException {
		// Arrange
		Call callMock = mock(Call.class);
		when(callMock.execute()).thenThrow(new IOException("Network error"));
		when(operationRestClient.updateTransaction(anyString(), any(V2TransactionUpdateDto.class)))
				.thenReturn(callMock);

		// Act & Assert - Should not throw exception
		operationAdapter.updateTransactionStatus(transactionId, "COMPLETED", userId);

		verify(operationRestClient).updateTransaction(anyString(), any(V2TransactionUpdateDto.class));
	}

	// ─────────────────────────────────────────────────────────────────────────
	// updateOperationStatus: Éxito y error
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void updateOperationStatus_whenSuccessful_shouldCallUpdateOperation() throws IOException {
		// Arrange
		Response<Void> response = Response.success(null);
		Call callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(operationRestClient.updateOperation(anyString(), any(V2OperationUpdateRequestDto.class)))
				.thenReturn(callMock);

		// Act
		operationAdapter.updateOperationStatus(operationId, "COMPLETED", userId);

		// Assert
		verify(operationRestClient).updateOperation(anyString(), any(V2OperationUpdateRequestDto.class));
	}

	@Test
	void updateOperationStatus_whenIOExceptionOccurs_shouldCatchAndNotThrow() throws IOException {
		// Arrange
		Call callMock = mock(Call.class);
		when(callMock.execute()).thenThrow(new IOException("Server error"));
		when(operationRestClient.updateOperation(anyString(), any(V2OperationUpdateRequestDto.class)))
				.thenReturn(callMock);

		// Act & Assert - Should not throw exception
		operationAdapter.updateOperationStatus(operationId, "COMPLETED", userId);

		verify(operationRestClient).updateOperation(anyString(), any(V2OperationUpdateRequestDto.class));
	}

	// ─────────────────────────────────────────────────────────────────────────
	// createOperation: Éxito
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void createOperation_whenSuccessful_shouldReturnMappedOperation() throws IOException {
		// Arrange
		V2OperationResponseDto operationDto = createV2OperationResponseDto();
		Response<V2OperationResponseDto> response = Response.success(operationDto);
		Call<V2OperationResponseDto> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(operationRestClient.createOperation(any(V2OperationCreateRequestDto.class))).thenReturn(callMock);

		// Act
		OperationResponse result = operationAdapter.createOperation(userId, accountId, BigDecimal.valueOf(100.00),
				"SERVICE_PAYMENT", "REC-001", "Origin Name", "Destination Name", "Description", "Detail",
				"{\"data\":\"additional\"}");

		// Assert
		assertNotNull(result);
		assertEquals("OP-001", result.getOperationId());
	}

	// ─────────────────────────────────────────────────────────────────────────
	// createOperation: Respuesta no exitosa
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void createOperation_whenResponseNotSuccessful_shouldThrowModelRestClientException() throws IOException {
		// Arrange
		Response<V2OperationResponseDto> response = Response.error(400, okBody());
		Call<V2OperationResponseDto> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(operationRestClient.createOperation(any(V2OperationCreateRequestDto.class))).thenReturn(callMock);

		// Act & Assert
		assertThrows(ModelRestClientException.class,
				() -> operationAdapter.createOperation(userId, accountId, BigDecimal.valueOf(100.00), "SERVICE_PAYMENT",
						"REC-001", "Origin Name", "Destination Name", "Description", "Detail", null));
	}

	// ─────────────────────────────────────────────────────────────────────────
	// createOperation: Cuerpo o ID nulo
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void createOperation_whenResponseBodyNull_shouldThrowExternalServiceException() throws IOException {
		// Arrange
		Response<V2OperationResponseDto> response = Response.success(null);
		Call<V2OperationResponseDto> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(operationRestClient.createOperation(any(V2OperationCreateRequestDto.class))).thenReturn(callMock);

		// Act & Assert
		ExternalServiceException exception = assertThrows(ExternalServiceException.class,
				() -> operationAdapter.createOperation(userId, accountId, BigDecimal.valueOf(100.00), "SERVICE_PAYMENT",
						"REC-001", "Origin Name", "Destination Name", "Description", "Detail", null));

		assertNotNull(exception.getMessage());
	}

	@Test
	void createOperation_whenOperationIdNull_shouldThrowExternalServiceException() throws IOException {
		// Arrange
		V2OperationResponseDto operationDto = createV2OperationResponseDto();
		operationDto.setOperationId(null);

		Response<V2OperationResponseDto> response = Response.success(operationDto);
		Call<V2OperationResponseDto> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(operationRestClient.createOperation(any(V2OperationCreateRequestDto.class))).thenReturn(callMock);

		// Act & Assert
		ExternalServiceException exception = assertThrows(ExternalServiceException.class,
				() -> operationAdapter.createOperation(userId, accountId, BigDecimal.valueOf(100.00), "SERVICE_PAYMENT",
						"REC-001", "Origin Name", "Destination Name", "Description", "Detail", null));

		assertNotNull(exception.getMessage());
	}

	@Test
	void createOperation_whenIOExceptionOccurs_shouldThrowExternalServiceException() throws IOException {
		// Arrange
		Call<V2OperationResponseDto> callMock = mock(Call.class);
		when(callMock.execute()).thenThrow(new IOException("Network error"));
		when(operationRestClient.createOperation(any(V2OperationCreateRequestDto.class))).thenReturn(callMock);

		// Act & Assert
		ExternalServiceException exception = assertThrows(ExternalServiceException.class,
				() -> operationAdapter.createOperation(userId, accountId, BigDecimal.valueOf(100.00), "SERVICE_PAYMENT",
						"REC-001", "Origin Name", "Destination Name", "Description", "Detail", null));

		assertNotNull(exception.getMessage());
	}

	// ─────────────────────────────────────────────────────────────────────────
	// createTransaction: Éxito
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void createTransaction_whenSuccessful_shouldReturnMappedTransaction() throws IOException {
		// Arrange
		V2TransactionDto transactionDto = createV2TransactionDto();
		Response<V2TransactionDto> response = Response.success(transactionDto);
		Call<V2TransactionDto> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(operationRestClient.createTransaction(any(V2TransactionCreateRequestDto.class))).thenReturn(callMock);

		// Act
		Transaction result = operationAdapter.createTransaction(operationId, userId, "CASH_OUT", "IBK",
				BigDecimal.valueOf(100.00), operationNumber);

		// Assert
		assertNotNull(result);
		assertEquals("TXN-001", result.getTransactionId());
		assertEquals("OP-001", result.getOperationId());
	}

	// ─────────────────────────────────────────────────────────────────────────
	// createTransaction: Respuesta no exitosa
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void createTransaction_whenResponseNotSuccessful_shouldThrowModelRestClientException() throws IOException {
		// Arrange
		Response<V2TransactionDto> response = Response.error(400, okBody());
		Call<V2TransactionDto> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(operationRestClient.createTransaction(any(V2TransactionCreateRequestDto.class))).thenReturn(callMock);

		// Act & Assert
		assertThrows(ModelRestClientException.class, () -> operationAdapter.createTransaction(operationId, userId,
				"CASH_OUT", "IBK", BigDecimal.valueOf(100.00), operationNumber));
	}

	// ─────────────────────────────────────────────────────────────────────────
	// createTransaction: Cuerpo o ID nulo
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void createTransaction_whenResponseBodyNull_shouldThrowExternalServiceException() throws IOException {
		// Arrange
		Response<V2TransactionDto> response = Response.success(null);
		Call<V2TransactionDto> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(operationRestClient.createTransaction(any(V2TransactionCreateRequestDto.class))).thenReturn(callMock);

		// Act & Assert
		ExternalServiceException exception = assertThrows(ExternalServiceException.class,
				() -> operationAdapter.createTransaction(operationId, userId, "CASH_OUT", "IBK",
						BigDecimal.valueOf(100.00), operationNumber));

		assertNotNull(exception.getMessage());
	}

	@Test
	void createTransaction_whenOperationIdNull_shouldThrowExternalServiceException() throws IOException {
		// Arrange
		V2TransactionDto transactionDto = createV2TransactionDto();
		transactionDto.setOperationId(null);

		Response<V2TransactionDto> response = Response.success(transactionDto);
		Call<V2TransactionDto> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(operationRestClient.createTransaction(any(V2TransactionCreateRequestDto.class))).thenReturn(callMock);

		// Act & Assert
		ExternalServiceException exception = assertThrows(ExternalServiceException.class,
				() -> operationAdapter.createTransaction(operationId, userId, "CASH_OUT", "IBK",
						BigDecimal.valueOf(100.00), operationNumber));

		assertNotNull(exception.getMessage());
	}

	@Test
	void createTransaction_whenIOExceptionOccurs_shouldThrowExternalServiceException() throws IOException {
		// Arrange
		Call<V2TransactionDto> callMock = mock(Call.class);
		when(callMock.execute()).thenThrow(new IOException("Network error"));
		when(operationRestClient.createTransaction(any(V2TransactionCreateRequestDto.class))).thenReturn(callMock);

		// Act & Assert
		ExternalServiceException exception = assertThrows(ExternalServiceException.class,
				() -> operationAdapter.createTransaction(operationId, userId, "CASH_OUT", "IBK",
						BigDecimal.valueOf(100.00), operationNumber));

		assertNotNull(exception.getMessage());
	}

	// ─────────────────────────────────────────────────────────────────────────
	// Helper methods
	// ─────────────────────────────────────────────────────────────────────────

	private V2TransactionDto createV2TransactionDto() {
		return V2TransactionDto.builder().transactionId("TXN-001").operationId("OP-001").status("COMPLETED")
				.createdUser("USER-001").amount(BigDecimal.valueOf(100.00)).build();
	}

	private V2OperationResponseDto createV2OperationResponseDto() {
		V2OperationResponseDto dto = new V2OperationResponseDto();
		dto.setOperationId("OP-001");
		dto.setOperationNumber("OP-001-NUM");
		dto.setOriginAccountId("ACC-001");
		dto.setDestinationAccountId("ACC-001");
		dto.setAmount(BigDecimal.valueOf(150.00));
		dto.setTipAmount(BigDecimal.ZERO);
		dto.setType("SERVICE_PAYMENT");
		dto.setDescription("Description");
		dto.setDetail("Detail");
		dto.setAdditionalData("{\"data\":\"additional\"}");
		dto.setStatus("IN_PROGRESS");
		return dto;
	}

	private okhttp3.ResponseBody okBody() {
		return okhttp3.ResponseBody.create("", okhttp3.MediaType.parse("application/json"));
	}
}
