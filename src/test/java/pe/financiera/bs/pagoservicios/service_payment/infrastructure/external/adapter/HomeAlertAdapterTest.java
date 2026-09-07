package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Alert;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.V2HomeRestClientAdapter;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.V2Alert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HomeAlertAdapterTest {

	private HomeAlertAdapter homeAlertAdapter;

	private V2HomeRestClientAdapter homeRestClientAdapter;

	private String userId;

	@BeforeEach
	void setUp() {
		homeRestClientAdapter = mock(V2HomeRestClientAdapter.class);
		homeAlertAdapter = new HomeAlertAdapter(homeRestClientAdapter);

		userId = "USER-001";
	}

	// ─────────────────────────────────────────────────────────────────────────
	// Éxito: Alerta encontrada y mapeada correctamente
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void getHomeAlert_whenAlertFound_shouldCallHomeRestClientAdapterWithCorrectUserId() {
		// Arrange
		V2Alert v2AlertMock = createV2Alert("Alert Title", "Alert Message", "INFO");
		when(homeRestClientAdapter.getHomeAlert(userId)).thenReturn(v2AlertMock);

		// Act
		Alert result = homeAlertAdapter.getHomeAlert(userId);

		// Assert
		assertNotNull(result);
		verify(homeRestClientAdapter).getHomeAlert(userId);
	}

	@Test
	void getHomeAlert_whenAlertFound_shouldMapTitleCorrectly() {
		// Arrange
		V2Alert v2AlertMock = createV2Alert("Welcome User", "Message content", "INFO");
		when(homeRestClientAdapter.getHomeAlert(userId)).thenReturn(v2AlertMock);

		// Act
		Alert result = homeAlertAdapter.getHomeAlert(userId);

		// Assert
		assertNotNull(result);
		assertEquals("Welcome User", result.getTitle());
	}

	@Test
	void getHomeAlert_whenAlertFound_shouldMapMessageCorrectly() {
		// Arrange
		V2Alert v2AlertMock = createV2Alert("Title", "This is a test message", "WARNING");
		when(homeRestClientAdapter.getHomeAlert(userId)).thenReturn(v2AlertMock);

		// Act
		Alert result = homeAlertAdapter.getHomeAlert(userId);

		// Assert
		assertNotNull(result);
		assertEquals("This is a test message", result.getMessage());
	}

	@Test
	void getHomeAlert_whenAlertFound_shouldMapTypeCorrectly() {
		// Arrange
		V2Alert v2AlertMock = createV2Alert("Title", "Message", "ERROR");
		when(homeRestClientAdapter.getHomeAlert(userId)).thenReturn(v2AlertMock);

		// Act
		Alert result = homeAlertAdapter.getHomeAlert(userId);

		// Assert
		assertNotNull(result);
		assertEquals("ERROR", result.getType());
	}

	@Test
	void getHomeAlert_whenAlertFound_shouldReturnMappedAlertWithAllFields() {
		// Arrange
		V2Alert v2AlertMock = createV2Alert("Important Alert", "Action required", "CRITICAL");
		when(homeRestClientAdapter.getHomeAlert(userId)).thenReturn(v2AlertMock);

		// Act
		Alert result = homeAlertAdapter.getHomeAlert(userId);

		// Assert
		assertNotNull(result);
		assertEquals("Important Alert", result.getTitle());
		assertEquals("Action required", result.getMessage());
		assertEquals("CRITICAL", result.getType());
	}

	// ─────────────────────────────────────────────────────────────────────────
	// Sin alerta encontrada: Resultado nulo
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void getHomeAlert_whenNoAlertFound_shouldReturnNull() {
		// Arrange
		when(homeRestClientAdapter.getHomeAlert(userId)).thenReturn(null);

		// Act
		Alert result = homeAlertAdapter.getHomeAlert(userId);

		// Assert
		assertNull(result);
	}

	@Test
	void getHomeAlert_whenNoAlertFound_shouldCallHomeRestClientAdapter() {
		// Arrange
		when(homeRestClientAdapter.getHomeAlert(userId)).thenReturn(null);

		// Act
		homeAlertAdapter.getHomeAlert(userId);

		// Assert
		verify(homeRestClientAdapter).getHomeAlert(userId);
	}

	@Test
	void getHomeAlert_whenNoAlertFound_shouldHandleNullGracefully() {
		// Arrange
		when(homeRestClientAdapter.getHomeAlert(userId)).thenReturn(null);

		// Act & Assert - Should not throw exception
		Alert result = homeAlertAdapter.getHomeAlert(userId);
		assertNull(result);
	}

	// ─────────────────────────────────────────────────────────────────────────
	// Helper methods to create test data
	// ─────────────────────────────────────────────────────────────────────────

	private V2Alert createV2Alert(String title, String message, String type) {
		V2Alert alert = new V2Alert();
		alert.setTitle(title);
		alert.setMessage(message);
		alert.setType(type);
		return alert;
	}
}
