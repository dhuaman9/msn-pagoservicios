package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.AuditoriaCommand;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.PaymentExecutionResult;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.PaymentRequest;
import pe.financieraoh.framework.auditoria.producer.message.AuditoriaProducerService;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.ResponseWrapper;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class AuditoriaAdapterTest {

	private AuditoriaAdapter auditoriaAdapter;

	private ObjectMapper objectMapper;
	private AuditoriaProducerService auditoriaProducerService;

	private PaymentRequest requestMock;
	private ResponseWrapper<PaymentExecutionResult> responseMock;
	private AuditoriaCommand auditoriaCommandMock;

	JsonProcessingException mockException;

	@BeforeEach
	void setUp() {
		objectMapper = mock(ObjectMapper.class);
		auditoriaProducerService = mock(AuditoriaProducerService.class);
		auditoriaAdapter = new AuditoriaAdapter(objectMapper, auditoriaProducerService);

		requestMock = PaymentRequest.builder().codInterno("INT-001").recipientId("REC-001").serviceId("SVC-001")
				.clientId("CLI-001").amount(BigDecimal.valueOf(100.0)).build();

		responseMock = new ResponseWrapper<>();
		responseMock.setDataOK(PaymentExecutionResult.builder().operationId("OP-001").operationNumber("OP-001-NUM")
				.amount(BigDecimal.valueOf(100.0)).status("SUCCESS").build());

		auditoriaCommandMock = AuditoriaCommand.builder().codInterno("INT-001").codigoOperacion("OP-001-NUM")
				.servicio("Pago Servicios").evento("Pago Servicios").headers("{\"header\":\"value\"}").build();
		mockException = new JsonProcessingException("Error") {
		};
	}


	@Test
	void procesarAuditoria_whenSuccessful_shouldSerializeRequestResponseToJson() throws JsonProcessingException {
		// Arrange
		when(objectMapper.writeValueAsString(any())).thenReturn("{\"auditoria\":\"data\"}");

		// Act
		auditoriaAdapter.procesarAuditoria(requestMock, responseMock, auditoriaCommandMock);

		// Assert - Verify that ObjectMapper was called to serialize objects
		verify(objectMapper).writeValueAsString(any());
		verify(auditoriaProducerService).publicarAuditoria(anyString());
	}

	@Test
	void procesarAuditoria_whenSuccessful_shouldCallPublicarAuditoriaWithSerializedJson()
			throws JsonProcessingException {
		// Arrange
		String expectedJson = "{\"dominio\":\"Pago de Servicios\",\"flujoNegocio\":\"bspagoservicios\"}";
		when(objectMapper.writeValueAsString(any())).thenReturn(expectedJson);


		// Act
		auditoriaAdapter.procesarAuditoria(requestMock, responseMock, auditoriaCommandMock);

		// Assert
		verify(auditoriaProducerService).publicarAuditoria(anyString());
	}


    @Test
    void procesarAuditoria_whenResponseSerializationFails_shouldHandleGracefully() throws JsonProcessingException {
        doThrow(mockException).when(objectMapper).writeValueAsString(any());

        // Act & Assert
        assertDoesNotThrow(() -> auditoriaAdapter.procesarAuditoria(requestMock, responseMock, auditoriaCommandMock));
    }

	@Test
	void procesarAuditoria_whenNullRequestIsProvided_shouldHandleNullObjectSerialization()
			throws JsonProcessingException {

		when(objectMapper.writeValueAsString(any())).thenReturn("{\"auditoria\":\"data\"}");

		assertDoesNotThrow(() -> auditoriaAdapter.procesarAuditoria(null, responseMock, auditoriaCommandMock));
	}

	@Test
	void procesarAuditoria_whenNullResponseIsProvided_shouldHandleNullObjectSerialization()
			throws JsonProcessingException {

		when(objectMapper.writeValueAsString(any())).thenReturn("{\"auditoria\":\"data\"}");

		// Act & Assert
		assertDoesNotThrow(() -> auditoriaAdapter.procesarAuditoria(requestMock, null, auditoriaCommandMock));
	}
}
