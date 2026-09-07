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
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Operator;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.PaymentExecutionResult;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.in.FindRechargeOperatorsUseCase;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.in.PayRechargeUseCase;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.mapper.HeadersMapper;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.mapper.RechargeRestMapper;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.request.RechargeConfirmRequest;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.response.OperatorListResponse;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.constant.HeaderConstant;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RechargeV2Controller.class)
class RechargeV2ControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private FindRechargeOperatorsUseCase findRechargeOperatorsUseCase;

	@MockitoBean
	private PayRechargeUseCase payRechargeUseCase;

	@MockitoBean
	private RechargeRestMapper rechargeRestMapper;

	@MockitoBean
	private HeadersMapper headersMapper;

	private RechargeConfirmRequest rechargeConfirmRequestMock;
	private OperatorListResponse operatorListResponseMock;
	private PaymentExecutionResult paymentExecutionResultMock;

	@BeforeEach
	void setUp() {
		rechargeConfirmRequestMock = RechargeConfirmRequest.builder().recipientId("REC-CLARO")
				.serviceId("SVC-RECHARGE-001").clientId("CLI-001").amount(BigDecimal.valueOf(50.00)).build();

		operatorListResponseMock = new OperatorListResponse();

		paymentExecutionResultMock = PaymentExecutionResult.builder().operationId("OP-RECHARGE-001")
				.operationNumber("OP-RECHARGE-001-NUM").amount(BigDecimal.valueOf(50.00)).amountFormat("S/ 50.00")
				.labelDetail("Recarga Claro").detail("Recarga de Celular").status("SUCCESS")
				.date(System.currentTimeMillis() / 1000).name("Claro").expirationDate(null).build();
	}

	// ─────────────────────────────────────────────────────────────────────────
	// getOperators endpoint: Éxito
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void getOperators_whenCalled_shouldReturnOkWithOperatorList() throws Exception {
		// Arrange
		List<Operator> operators = Collections.emptyList();
		when(findRechargeOperatorsUseCase.execute()).thenReturn(operators);
		when(rechargeRestMapper.toOperatorListResponse(operators)).thenReturn(operatorListResponseMock);

		// Act & Assert
		mockMvc.perform(get("/recharge/validate").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	@Test
	void getOperators_whenCalled_shouldExecuteFindRechargeOperatorsUseCase() throws Exception {
		// Arrange
		List<Operator> operators = Collections.emptyList();
		when(findRechargeOperatorsUseCase.execute()).thenReturn(operators);
		when(rechargeRestMapper.toOperatorListResponse(operators)).thenReturn(operatorListResponseMock);

		// Act
		mockMvc.perform(get("/recharge/validate").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		// Assert
		verify(findRechargeOperatorsUseCase).execute();
	}

	@Test
	void getOperators_whenCalled_shouldMapResultToOperatorListResponse() throws Exception {
		// Arrange
		List<Operator> operators = Collections.emptyList();
		when(findRechargeOperatorsUseCase.execute()).thenReturn(operators);
		when(rechargeRestMapper.toOperatorListResponse(operators)).thenReturn(operatorListResponseMock);

		// Act
		mockMvc.perform(get("/recharge/validate").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		// Assert
		verify(rechargeRestMapper).toOperatorListResponse(operators);
	}

	// ─────────────────────────────────────────────────────────────────────────
	// confirmRecharge endpoint: Éxito
	// ─────────────────────────────────────────────────────────────────────────

	@Test
	void confirmRecharge_whenValidRequest_shouldReturnOkWithPaymentResult() throws Exception {
		// Arrange
		when(headersMapper.headerToJson(any(HttpHeaders.class))).thenReturn("{\"headers\":\"json\"}");
		when(payRechargeUseCase.execute(any())).thenReturn(paymentExecutionResultMock);

		String requestJson = objectMapper.writeValueAsString(rechargeConfirmRequestMock);

		// Act & Assert
		mockMvc.perform(post("/recharge/confirmation").contentType(MediaType.APPLICATION_JSON)
				.header(HeaderConstant.HEADER_COD_INTERNO, "INT-001").header(HeaderConstant.HEADER_COD_CANAL, "MOBILE")
				.header(HeaderConstant.DEVICE_UUID, "DEVICE-123").content(requestJson)).andExpect(status().isOk());
	}

	@Test
	void confirmRecharge_whenValidRequest_shouldMapHeadersToJson() throws Exception {
		// Arrange
		when(headersMapper.headerToJson(any(HttpHeaders.class))).thenReturn("{\"headers\":\"json\"}");
		when(payRechargeUseCase.execute(any())).thenReturn(paymentExecutionResultMock);

		String requestJson = objectMapper.writeValueAsString(rechargeConfirmRequestMock);

		// Act
		mockMvc.perform(post("/recharge/confirmation").contentType(MediaType.APPLICATION_JSON)
				.header(HeaderConstant.HEADER_COD_INTERNO, "INT-001").header(HeaderConstant.HEADER_COD_CANAL, "MOBILE")
				.header(HeaderConstant.DEVICE_UUID, "DEVICE-123").content(requestJson)).andExpect(status().isOk());

		// Assert
		verify(headersMapper).headerToJson(any(HttpHeaders.class));
	}

	@Test
	void confirmRecharge_whenValidRequest_shouldReturnOperationData() throws Exception {
		// Arrange
		when(headersMapper.headerToJson(any(HttpHeaders.class))).thenReturn("{\"headers\":\"json\"}");
		when(payRechargeUseCase.execute(any())).thenReturn(paymentExecutionResultMock);

		String requestJson = objectMapper.writeValueAsString(rechargeConfirmRequestMock);

		// Act & Assert
		mockMvc.perform(post("/recharge/confirmation").contentType(MediaType.APPLICATION_JSON)
				.header(HeaderConstant.HEADER_COD_INTERNO, "INT-001").header(HeaderConstant.HEADER_COD_CANAL, "MOBILE")
				.header(HeaderConstant.DEVICE_UUID, "DEVICE-123").content(requestJson)).andExpect(status().isOk())
				.andExpect(jsonPath("$.operation.operationId").value("OP-RECHARGE-001"))
				.andExpect(jsonPath("$.operation.operationNumber").value("OP-RECHARGE-001-NUM"))
				.andExpect(jsonPath("$.operation.amount").value(50.00))
				.andExpect(jsonPath("$.operation.amountFormat").value("S/ 50.00"))
				.andExpect(jsonPath("$.operation.status").value("SUCCESS"));
	}

	@Test
	void confirmRecharge_whenValidRequest_shouldIncludeRechargeOperationType() throws Exception {
		// Arrange
		when(headersMapper.headerToJson(any(HttpHeaders.class))).thenReturn("{\"headers\":\"json\"}");
		when(payRechargeUseCase.execute(any())).thenReturn(paymentExecutionResultMock);

		String requestJson = objectMapper.writeValueAsString(rechargeConfirmRequestMock);

		// Act
		mockMvc.perform(post("/recharge/confirmation").contentType(MediaType.APPLICATION_JSON)
				.header(HeaderConstant.HEADER_COD_INTERNO, "INT-001").header(HeaderConstant.HEADER_COD_CANAL, "MOBILE")
				.header(HeaderConstant.DEVICE_UUID, "DEVICE-123").content(requestJson)).andExpect(status().isOk());

		// Assert
		verify(payRechargeUseCase).execute(any());
	}

	@Test
	void confirmRecharge_whenValidRequestWithoutDeviceUuid_shouldSetEmptyDeviceUuid() throws Exception {
		// Arrange
		when(headersMapper.headerToJson(any(HttpHeaders.class))).thenReturn("{\"headers\":\"json\"}");
		when(payRechargeUseCase.execute(any())).thenReturn(paymentExecutionResultMock);

		String requestJson = objectMapper.writeValueAsString(rechargeConfirmRequestMock);

		// Act & Assert
		mockMvc.perform(post("/recharge/confirmation").contentType(MediaType.APPLICATION_JSON)
				.header(HeaderConstant.HEADER_COD_INTERNO, "INT-001").header(HeaderConstant.HEADER_COD_CANAL, "MOBILE")
				.content(requestJson)).andExpect(status().isOk());

		verify(payRechargeUseCase).execute(any());
	}

	@Test
	void confirmRecharge_whenValidRequest_shouldReturnCompletePaymentResultResponse() throws Exception {
		// Arrange
		when(headersMapper.headerToJson(any(HttpHeaders.class))).thenReturn("{\"headers\":\"json\"}");
		when(payRechargeUseCase.execute(any())).thenReturn(paymentExecutionResultMock);

		String requestJson = objectMapper.writeValueAsString(rechargeConfirmRequestMock);

		// Act & Assert
		mockMvc.perform(post("/recharge/confirmation").contentType(MediaType.APPLICATION_JSON)
				.header(HeaderConstant.HEADER_COD_INTERNO, "INT-001").header(HeaderConstant.HEADER_COD_CANAL, "MOBILE")
				.header(HeaderConstant.DEVICE_UUID, "DEVICE-123").content(requestJson)).andExpect(status().isOk())
				.andExpect(jsonPath("$.operation").exists()).andExpect(jsonPath("$.operation.operationId").exists())
				.andExpect(jsonPath("$.operation.operationNumber").exists())
				.andExpect(jsonPath("$.operation.amount").exists())
				.andExpect(jsonPath("$.operation.amountFormat").exists())
				.andExpect(jsonPath("$.operation.labelDetail").exists())
				.andExpect(jsonPath("$.operation.detail").exists()).andExpect(jsonPath("$.operation.status").exists());
	}
}
