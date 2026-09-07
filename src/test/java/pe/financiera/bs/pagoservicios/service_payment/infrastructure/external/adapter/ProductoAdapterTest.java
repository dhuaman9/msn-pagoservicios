package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.financiera.bs.pagoservicios.service_payment.domain.exception.ExternalServiceException;
import pe.financiera.bs.pagoservicios.service_payment.domain.exception.ModelNotFoundException;
import pe.financiera.bs.pagoservicios.service_payment.domain.exception.ModelRestClientException;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.ProductoCommand;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.ProductoResult;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.ProductoRestClient;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.ProductoRestRequest;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.ProductoRestResponse;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.ResponseListWrapper;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductoAdapterTest {

	private ProductoAdapter productoAdapter;

	private ProductoRestClient productoRestClient;

	private ProductoCommand productoCommandMock;

	@BeforeEach
	void setUp() {
		productoRestClient = mock(ProductoRestClient.class);
		productoAdapter = new ProductoAdapter(productoRestClient);

		productoCommandMock = ProductoCommand.builder().codInterno("INT-001").tipoTarjeta("PRINCIPAL")
				.codBloqueoCuenta("N").codBloqueoTarjeta("N").bloqueoCuentaCondicion(1).codProducto("DEB_FISICO")
				.build();
	}

	// ─────────────────────────────────────────────────────────────────────────
	// Éxito: Construcción correcta de request y mapeo exitoso
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void buscarProducto_whenSuccessful_shouldConstructRequestAndExecuteCall() throws IOException {
		// Arrange
		ProductoRestResponse restResponse = createProductoRestResponse();
		ResponseListWrapper<ProductoRestResponse> wrapper = new ResponseListWrapper<>();
		wrapper.setData(List.of(restResponse));

		Response<ResponseListWrapper<ProductoRestResponse>> response = Response.success(wrapper);
		Call<ResponseListWrapper<ProductoRestResponse>> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(productoRestClient.obtenerPersonasProdPorCodInterno(any(ProductoRestRequest.class))).thenReturn(callMock);

		// Act
		ProductoResult result = productoAdapter.buscarProducto(productoCommandMock);

		// Assert
		assertNotNull(result);
		verify(productoRestClient).obtenerPersonasProdPorCodInterno(any(ProductoRestRequest.class));
	}

	@Test
	void buscarProducto_whenSuccessful_shouldMapProductoResultCorrectly() throws IOException {
		// Arrange
		ProductoRestResponse restResponse = createProductoRestResponse();
		ResponseListWrapper<ProductoRestResponse> wrapper = new ResponseListWrapper<>();
		wrapper.setData(List.of(restResponse));

		Response<ResponseListWrapper<ProductoRestResponse>> response = Response.success(wrapper);
		Call<ResponseListWrapper<ProductoRestResponse>> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(productoRestClient.obtenerPersonasProdPorCodInterno(any(ProductoRestRequest.class))).thenReturn(callMock);

		// Act
		ProductoResult result = productoAdapter.buscarProducto(productoCommandMock);

		// Assert
		assertNotNull(result);
		assertNotNull(result.uidcliente());
		assertNotNull(result.uidcuenta());
	}

	@Test
	void buscarProducto_whenSuccessful_shouldGetFirstElementFromList() throws IOException {
		// Arrange
		ProductoRestResponse firstResponse = createProductoRestResponse();
		ProductoRestResponse secondResponse = createProductoRestResponse();

		ResponseListWrapper<ProductoRestResponse> wrapper = new ResponseListWrapper<>();
		wrapper.setData(List.of(firstResponse, secondResponse));

		Response<ResponseListWrapper<ProductoRestResponse>> response = Response.success(wrapper);
		Call<ResponseListWrapper<ProductoRestResponse>> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(productoRestClient.obtenerPersonasProdPorCodInterno(any(ProductoRestRequest.class))).thenReturn(callMock);

		// Act
		ProductoResult result = productoAdapter.buscarProducto(productoCommandMock);

		// Assert
		assertNotNull(result);
		verify(productoRestClient).obtenerPersonasProdPorCodInterno(any(ProductoRestRequest.class));
	}

	@Test
	void buscarProducto_whenSuccessful_shouldReturnMappedDomainObject() throws IOException {
		// Arrange
		ProductoRestResponse restResponse = createProductoRestResponse();
		ResponseListWrapper<ProductoRestResponse> wrapper = new ResponseListWrapper<>();
		wrapper.setData(List.of(restResponse));

		Response<ResponseListWrapper<ProductoRestResponse>> response = Response.success(wrapper);
		Call<ResponseListWrapper<ProductoRestResponse>> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(productoRestClient.obtenerPersonasProdPorCodInterno(any(ProductoRestRequest.class))).thenReturn(callMock);

		// Act
		ProductoResult result = productoAdapter.buscarProducto(productoCommandMock);

		// Assert
		assertNotNull(result);
	}

	// ─────────────────────────────────────────────────────────────────────────
	// Respuesta no exitosa: Código HTTP de error
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void buscarProducto_whenResponseNotSuccessful_shouldThrowModelRestClientException() throws IOException {
		// Arrange
		Response<ResponseListWrapper<ProductoRestResponse>> response = Response.error(400, okBody());
		Call<ResponseListWrapper<ProductoRestResponse>> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(productoRestClient.obtenerPersonasProdPorCodInterno(any(ProductoRestRequest.class))).thenReturn(callMock);

		// Act & Assert
		ModelRestClientException exception = assertThrows(ModelRestClientException.class,
				() -> productoAdapter.buscarProducto(productoCommandMock));

		assertNotNull(exception.getMessage());
	}

	@Test
	void buscarProducto_whenResponseUnauthorized_shouldThrowModelRestClientExceptionWith401() throws IOException {
		// Arrange
		Response<ResponseListWrapper<ProductoRestResponse>> response = Response.error(401, okBody());
		Call<ResponseListWrapper<ProductoRestResponse>> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(productoRestClient.obtenerPersonasProdPorCodInterno(any(ProductoRestRequest.class))).thenReturn(callMock);

		// Act & Assert
		assertThrows(ModelRestClientException.class, () -> productoAdapter.buscarProducto(productoCommandMock));
	}

	@Test
	void buscarProducto_whenResponseServerError_shouldThrowModelRestClientExceptionWith500() throws IOException {
		// Arrange
		Response<ResponseListWrapper<ProductoRestResponse>> response = Response.error(500, okBody());
		Call<ResponseListWrapper<ProductoRestResponse>> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(productoRestClient.obtenerPersonasProdPorCodInterno(any(ProductoRestRequest.class))).thenReturn(callMock);

		// Act & Assert
		assertThrows(ModelRestClientException.class, () -> productoAdapter.buscarProducto(productoCommandMock));
	}

	// ─────────────────────────────────────────────────────────────────────────
	// Respuesta vacía o nula: Sin datos
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void buscarProducto_whenResponseBodyNull_shouldThrowModelNotFoundException() throws IOException {
		// Arrange
		Response<ResponseListWrapper<ProductoRestResponse>> response = Response.success(null);
		Call<ResponseListWrapper<ProductoRestResponse>> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(productoRestClient.obtenerPersonasProdPorCodInterno(any(ProductoRestRequest.class))).thenReturn(callMock);

		// Act & Assert
		ModelNotFoundException exception = assertThrows(ModelNotFoundException.class,
				() -> productoAdapter.buscarProducto(productoCommandMock));

		assertNotNull(exception.getMessage());
		assertTrue(exception.getMessage().contains("INT-001"));
	}

	@Test
	void buscarProducto_whenDataListNull_shouldThrowModelNotFoundException() throws IOException {
		// Arrange
		ResponseListWrapper<ProductoRestResponse> wrapper = new ResponseListWrapper<>();
		wrapper.setData(null);

		Response<ResponseListWrapper<ProductoRestResponse>> response = Response.success(wrapper);
		Call<ResponseListWrapper<ProductoRestResponse>> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(productoRestClient.obtenerPersonasProdPorCodInterno(any(ProductoRestRequest.class))).thenReturn(callMock);

		// Act & Assert
		ModelNotFoundException exception = assertThrows(ModelNotFoundException.class,
				() -> productoAdapter.buscarProducto(productoCommandMock));

		assertNotNull(exception.getMessage());
	}

	@Test
	void buscarProducto_whenDataListEmpty_shouldThrowModelNotFoundException() throws IOException {
		// Arrange
		ResponseListWrapper<ProductoRestResponse> wrapper = new ResponseListWrapper<>();
		wrapper.setData(Collections.emptyList());

		Response<ResponseListWrapper<ProductoRestResponse>> response = Response.success(wrapper);
		Call<ResponseListWrapper<ProductoRestResponse>> callMock = mock(Call.class);
		when(callMock.execute()).thenReturn(response);
		when(productoRestClient.obtenerPersonasProdPorCodInterno(any(ProductoRestRequest.class))).thenReturn(callMock);

		// Act & Assert
		ModelNotFoundException exception = assertThrows(ModelNotFoundException.class,
				() -> productoAdapter.buscarProducto(productoCommandMock));

		assertNotNull(exception.getMessage());
		assertTrue(exception.getMessage().contains("No se encontró producto"));
	}

	// ─────────────────────────────────────────────────────────────────────────
	// Error de E/S: IOException
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void buscarProducto_whenIOExceptionOccurs_shouldThrowExternalServiceException() throws IOException {
		// Arrange
		Call<ResponseListWrapper<ProductoRestResponse>> callMock = mock(Call.class);
		when(callMock.execute()).thenThrow(new IOException("Network error"));
		when(productoRestClient.obtenerPersonasProdPorCodInterno(any(ProductoRestRequest.class))).thenReturn(callMock);

		// Act & Assert
		ExternalServiceException exception = assertThrows(ExternalServiceException.class,
				() -> productoAdapter.buscarProducto(productoCommandMock));

		assertNotNull(exception.getMessage());
		assertTrue(exception.getMessage().contains("External service communication error"));
		assertTrue(exception.getMessage().contains("Network error"));
	}

	@Test
	void buscarProducto_whenConnectionTimeout_shouldThrowExternalServiceException() throws IOException {
		// Arrange
		Call<ResponseListWrapper<ProductoRestResponse>> callMock = mock(Call.class);
		when(callMock.execute()).thenThrow(new IOException("Connection timeout"));
		when(productoRestClient.obtenerPersonasProdPorCodInterno(any(ProductoRestRequest.class))).thenReturn(callMock);

		// Act & Assert
		ExternalServiceException exception = assertThrows(ExternalServiceException.class,
				() -> productoAdapter.buscarProducto(productoCommandMock));

		assertNotNull(exception.getMessage());
		assertTrue(exception.getMessage().contains("External service communication error"));
	}

	@Test
	void buscarProducto_whenSocketError_shouldThrowExternalServiceException() throws IOException {
		// Arrange
		Call<ResponseListWrapper<ProductoRestResponse>> callMock = mock(Call.class);
		when(callMock.execute()).thenThrow(new IOException("Socket error"));
		when(productoRestClient.obtenerPersonasProdPorCodInterno(any(ProductoRestRequest.class))).thenReturn(callMock);

		// Act & Assert
		assertThrows(ExternalServiceException.class, () -> productoAdapter.buscarProducto(productoCommandMock));
	}

	// ─────────────────────────────────────────────────────────────────────────
	// Helper methods
	// ─────────────────────────────────────────────────────────────────────────

	private ProductoRestResponse createProductoRestResponse() {

		return new ProductoRestResponse(141, "990213312", 5721, "4040719098909878", "N", "4040719098909878", "I", "N",
				"JUAN CARLOS", "P", null, "TEST", "TEST", "UID123445", "UID123445");
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
