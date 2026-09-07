package pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.OperationType;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.PaymentRequest;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.in.FindRechargeOperatorsUseCase;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.in.PayRechargeUseCase;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.advice.model.ErrorResponse;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.mapper.HeadersMapper;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.mapper.RechargeRestMapper;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.request.RechargeConfirmRequest;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.response.OperatorListResponse;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.response.PaymentOperation;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.response.PaymentResultResponse;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.constant.HeaderConstant;
import static pe.financiera.bs.pagoservicios.service_payment.infrastructure.constant.Constant.DATA_OK;

@Slf4j
@RestController
@RequestMapping("/recharge")
@RequiredArgsConstructor
public class RechargeV2Controller {

	private final FindRechargeOperatorsUseCase findRechargeOperatorsUseCase;
	private final PayRechargeUseCase payRechargeUseCase;
	private final RechargeRestMapper rechargeRestMapper;
	private final HeadersMapper headersMapper;

	@Operation(summary = "get operators")
	@GetMapping("/validate")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = DATA_OK, content = @Content(schema = @Schema(implementation = OperatorListResponse.class))),
			@ApiResponse(responseCode = "500", description = "Error en el servidor", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
	public ResponseEntity<OperatorListResponse> getOperators() {
		return ResponseEntity.ok(rechargeRestMapper.toOperatorListResponse(findRechargeOperatorsUseCase.execute()));
	}

	@Operation(summary = "Recharge confirmation")
	@PostMapping("/confirmation")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = DATA_OK, content = @Content(schema = @Schema(implementation = PaymentResultResponse.class))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "500", description = "Error en el servidor", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
	public ResponseEntity<PaymentResultResponse> confirmRecharge(@RequestHeader HttpHeaders headers,
			@RequestHeader(HeaderConstant.HEADER_COD_INTERNO) String codigoInterno,
			@RequestHeader(HeaderConstant.HEADER_COD_CANAL) String codCanal,
			@RequestHeader(value = HeaderConstant.DEVICE_UUID, required = false) String deviceUuid,
			@RequestBody RechargeConfirmRequest request) {

		String headersAsJson = headersMapper.headerToJson(headers);

		PaymentRequest paymentRequest = PaymentRequest.builder().codInterno(codigoInterno)
				.recipientId(request.getRecipientId()).serviceId(request.getServiceId()).clientId(request.getClientId())
				.amount(request.getAmount()).deviceUUID(deviceUuid != null ? deviceUuid : "")
				.operationType(OperationType.RECHARGE).headersAsJson(headersAsJson).build();

		var result = payRechargeUseCase.execute(paymentRequest);

		return ResponseEntity.ok(PaymentResultResponse.builder()
				.operation(PaymentOperation.builder().operationId(result.getOperationId())
						.operationNumber(result.getOperationNumber()).amount(result.getAmount())
						.amountFormat(result.getAmountFormat()).labelDetail(result.getLabelDetail())
						.detail(result.getDetail()).status(result.getStatus()).date(result.getDate())
						.name(result.getName()).expirationDate(result.getExpirationDate()).build())
				.build());
	}
}
