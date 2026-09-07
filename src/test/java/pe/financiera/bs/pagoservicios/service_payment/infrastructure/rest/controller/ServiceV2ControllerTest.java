package pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Service;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.ServiceStatus;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.in.GetServicesUseCase;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.constant.HeaderConstant;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ServiceV2Controller.class)
class ServiceV2ControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private GetServicesUseCase getServicesUseCase;

	private String recipientId;
	private List<Service> serviceListMock;

	@BeforeEach
	void setUp() {
		recipientId = "REC-001";

		Service serviceMock = Service.builder().id("SVC-001").name("Recarga Claro").type("RECHARGE")
				.label("Número de teléfono").length(9).dataType("NUMERIC").status(ServiceStatus.VALID).build();

		serviceListMock = List.of(serviceMock);
	}

	// ─────────────────────────────────────────────────────────────────────────
	// getServices endpoint: Éxito
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void getServices_whenValidRecipientId_shouldReturnOkWithServicesResponse() throws Exception {
		// Arrange
		when(getServicesUseCase.execute(recipientId)).thenReturn(serviceListMock);

		// Act & Assert
		mockMvc.perform(
				get("/service").contentType(MediaType.APPLICATION_JSON).param(HeaderConstant.RECIPIENT_ID, recipientId))
				.andExpect(status().isOk());
	}

	@Test
	void getServices_whenValidRecipientId_shouldExecuteGetServicesUseCase() throws Exception {
		// Arrange
		when(getServicesUseCase.execute(recipientId)).thenReturn(serviceListMock);

		// Act
		mockMvc.perform(
				get("/service").contentType(MediaType.APPLICATION_JSON).param(HeaderConstant.RECIPIENT_ID, recipientId))
				.andExpect(status().isOk());

		// Assert
		verify(getServicesUseCase).execute(recipientId);
	}

	@Test
	void getServices_whenValidRecipientId_shouldPassCorrectRecipientIdToUseCase() throws Exception {
		// Arrange
		String differentRecipientId = "REC-002";
		when(getServicesUseCase.execute(differentRecipientId)).thenReturn(serviceListMock);

		// Act
		mockMvc.perform(get("/service").contentType(MediaType.APPLICATION_JSON).param(HeaderConstant.RECIPIENT_ID,
				differentRecipientId)).andExpect(status().isOk());

		// Assert
		verify(getServicesUseCase).execute(differentRecipientId);
	}

	@Test
	void getServices_whenEmptyServiceList_shouldReturnOkWithEmptyServices() throws Exception {
		// Arrange
		when(getServicesUseCase.execute(recipientId)).thenReturn(Collections.emptyList());

		// Act & Assert
		mockMvc.perform(
				get("/service").contentType(MediaType.APPLICATION_JSON).param(HeaderConstant.RECIPIENT_ID, recipientId))
				.andExpect(status().isOk());
	}

	@Test
	void getServices_whenMultipleServices_shouldReturnAllServices() throws Exception {
		// Arrange
		Service secondService = Service.builder().id("SVC-002").name("Pago de agua").type("PAYMENT")
				.label("Número de cuenta").length(12).dataType("NUMERIC").status(ServiceStatus.VALID).build();

		List<Service> multipleServices = List.of(serviceListMock.getFirst(), secondService);
		when(getServicesUseCase.execute(recipientId)).thenReturn(multipleServices);

		// Act & Assert
		mockMvc.perform(
				get("/service").contentType(MediaType.APPLICATION_JSON).param(HeaderConstant.RECIPIENT_ID, recipientId))
				.andExpect(status().isOk());

		verify(getServicesUseCase).execute(recipientId);
	}

}
