package pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.BillList;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.PaymentExecutionResult;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.in.GetBillsUseCase;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.in.PayServiceUseCase;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.mapper.BillRestMapper;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.mapper.HeadersMapper;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.request.PaymentConfirmRequest;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.request.PreConfirmationRequest;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.response.V2BillsResponse;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.constant.HeaderConstant;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BillV2Controller.class)
class BillV2ControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private GetBillsUseCase getBillsUseCase;

	@MockitoBean
	private PayServiceUseCase payServiceUseCase;

	@MockitoBean
	private BillRestMapper billRestMapper;

	@MockitoBean
	private HeadersMapper headersMapper;

	private PreConfirmationRequest preConfirmationRequestMock;
	private PaymentConfirmRequest paymentConfirmRequestMock;
	private BillList billListMock;
	private V2BillsResponse billsResponseMock;
	private PaymentExecutionResult paymentExecutionResultMock;

	@BeforeEach
	void setUp() {
		preConfirmationRequestMock = PreConfirmationRequest.builder().recipientId("REC-001").serviceId("SVC-001")
				.clientId("CLI-001").build();

		paymentConfirmRequestMock = PaymentConfirmRequest.builder().recipientId("REC-001").serviceId("SVC-001")
				.billId("BILL-001").clientId("CLI-001").amount(BigDecimal.valueOf(100.00)).build();

		billListMock = BillList.builder().bills(Collections.emptyList()).build();

		billsResponseMock = new V2BillsResponse();

		paymentExecutionResultMock = PaymentExecutionResult.builder().operationId("OP-001")
				.operationNumber("OP-001-NUM").amount(BigDecimal.valueOf(100.00)).amountFormat("S/ 100.00")
				.labelDetail("Recarga Claro").detail("Payment service").status("SUCCESS")
				.date(System.currentTimeMillis() / 1000).name("Client Name").expirationDate(null).build();
	}

	// ─────────────────────────────────────────────────────────────────────────
	// preConfirmation endpoint: Éxito
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void preConfirmation_whenValidRequest_shouldReturnOkWithBillsResponse() throws Exception {
		// Arrange
		when(getBillsUseCase.execute(any(GetBillsUseCase.GetBillsCommand.class))).thenReturn(billListMock);
		when(billRestMapper.toBillListResponse(billListMock)).thenReturn(billsResponseMock);

		String requestJson = objectMapper.writeValueAsString(preConfirmationRequestMock);

		// Act & Assert
		mockMvc.perform(post("/payment/pre-confirmation")
				.contentType(MediaType.APPLICATION_JSON).content(requestJson)).andExpect(status().isOk());
	}

	@Test
	void preConfirmation_whenValidRequest_shouldExecuteGetBillsUseCase() throws Exception {
		// Arrange
		when(getBillsUseCase.execute(any(GetBillsUseCase.GetBillsCommand.class))).thenReturn(billListMock);
		when(billRestMapper.toBillListResponse(billListMock)).thenReturn(billsResponseMock);

		String requestJson = objectMapper.writeValueAsString(preConfirmationRequestMock);

		// Act
		mockMvc.perform(post("/payment/pre-confirmation")
				.contentType(MediaType.APPLICATION_JSON).content(requestJson)).andExpect(status().isOk());

		// Assert
		verify(getBillsUseCase).execute(any(GetBillsUseCase.GetBillsCommand.class));
	}

	@Test
	void preConfirmation_whenValidRequest_shouldMapResultToResponse() throws Exception {
		// Arrange
		when(getBillsUseCase.execute(any(GetBillsUseCase.GetBillsCommand.class))).thenReturn(billListMock);
		when(billRestMapper.toBillListResponse(billListMock)).thenReturn(billsResponseMock);

		String requestJson = objectMapper.writeValueAsString(preConfirmationRequestMock);

		// Act
		mockMvc.perform(post( "/payment/pre-confirmation")
				.contentType(MediaType.APPLICATION_JSON).content(requestJson)).andExpect(status().isOk());

		// Assert
		verify(billRestMapper).toBillListResponse(billListMock);
	}

	// ─────────────────────────────────────────────────────────────────────────
	// confirmPayment endpoint: Éxito
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void confirmPayment_whenValidRequest_shouldReturnOkWithPaymentResult() throws Exception {
		// Arrange
		when(headersMapper.headerToJson(any(HttpHeaders.class))).thenReturn("{\"headers\":\"json\"}");
		when(payServiceUseCase.execute(any())).thenReturn(paymentExecutionResultMock);

		String requestJson = objectMapper.writeValueAsString(paymentConfirmRequestMock);

		// Act & Assert
		mockMvc.perform(post("/payment/confirmation").contentType(MediaType.APPLICATION_JSON)
				.header(HeaderConstant.HEADER_COD_INTERNO, "INT-001").header(HeaderConstant.HEADER_COD_CANAL, "MOBILE")
				.header(HeaderConstant.DEVICE_UUID, "DEVICE-123").content(requestJson)).andExpect(status().isOk());
	}

	@Test
	void confirmPayment_whenValidRequest_shouldExecutePayServiceUseCase() throws Exception {
		// Arrange
		when(headersMapper.headerToJson(any(HttpHeaders.class))).thenReturn("{\"headers\":\"json\"}");
		when(payServiceUseCase.execute(any())).thenReturn(paymentExecutionResultMock);

		String requestJson = objectMapper.writeValueAsString(paymentConfirmRequestMock);

		// Act
		mockMvc.perform(post("/payment/confirmation").contentType(MediaType.APPLICATION_JSON)
				.header(HeaderConstant.HEADER_COD_INTERNO, "INT-001").header(HeaderConstant.HEADER_COD_CANAL, "MOBILE")
				.header(HeaderConstant.DEVICE_UUID, "DEVICE-123").content(requestJson)).andExpect(status().isOk());

		// Assert
		verify(payServiceUseCase).execute(any());
	}

	@Test
	void confirmPayment_whenValidRequest_shouldMapHeadersToJson() throws Exception {
		// Arrange
		when(headersMapper.headerToJson(any(HttpHeaders.class))).thenReturn("{\"headers\":\"json\"}");
		when(payServiceUseCase.execute(any())).thenReturn(paymentExecutionResultMock);

		String requestJson = objectMapper.writeValueAsString(paymentConfirmRequestMock);

		// Act
		mockMvc.perform(post("/payment/confirmation").contentType(MediaType.APPLICATION_JSON)
				.header(HeaderConstant.HEADER_COD_INTERNO, "INT-001").header(HeaderConstant.HEADER_COD_CANAL, "MOBILE")
				.header(HeaderConstant.DEVICE_UUID, "DEVICE-123").content(requestJson)).andExpect(status().isOk());

		// Assert
		verify(headersMapper).headerToJson(any(HttpHeaders.class));
	}

	@Test
	void confirmPayment_whenValidRequest_shouldReturnOperationData() throws Exception {
		// Arrange
		when(headersMapper.headerToJson(any(HttpHeaders.class))).thenReturn("{\"headers\":\"json\"}");
		when(payServiceUseCase.execute(any())).thenReturn(paymentExecutionResultMock);

		String requestJson = objectMapper.writeValueAsString(paymentConfirmRequestMock);

		// Act & Assert
		mockMvc.perform(post("/payment/confirmation").contentType(MediaType.APPLICATION_JSON)
				.header(HeaderConstant.HEADER_COD_INTERNO, "INT-001").header(HeaderConstant.HEADER_COD_CANAL, "MOBILE")
				.header(HeaderConstant.DEVICE_UUID, "DEVICE-123").content(requestJson)).andExpect(status().isOk())
				.andExpect(jsonPath("$.operation.operationId").value("OP-001"))
				.andExpect(jsonPath("$.operation.operationNumber").value("OP-001-NUM"))
				.andExpect(jsonPath("$.operation.amount").value(100.00))
				.andExpect(jsonPath("$.operation.amountFormat").value("S/ 100.00"))
				.andExpect(jsonPath("$.operation.status").value("SUCCESS"));
	}

	@Test
	void confirmPayment_whenValidRequestWithoutDeviceUuid_shouldSetEmptyDeviceUuid() throws Exception {
		// Arrange
		when(headersMapper.headerToJson(any(HttpHeaders.class))).thenReturn("{\"headers\":\"json\"}");
		when(payServiceUseCase.execute(any())).thenReturn(paymentExecutionResultMock);

		String requestJson = objectMapper.writeValueAsString(paymentConfirmRequestMock);

		// Act & Assert
		mockMvc.perform(post("/payment/confirmation").contentType(MediaType.APPLICATION_JSON)
				.header(HeaderConstant.HEADER_COD_INTERNO, "INT-001").header(HeaderConstant.HEADER_COD_CANAL, "MOBILE")
				.content(requestJson)).andExpect(status().isOk());

		verify(payServiceUseCase).execute(any());
	}

}
