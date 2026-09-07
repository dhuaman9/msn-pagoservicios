package pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.RecipientDashboard;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.in.FindLastRecipientsUseCase;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.advice.model.ErrorResponse;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.mapper.RecipientRestMapper;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.response.V2RecipientsResponse;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.constant.HeaderConstant;

import static pe.financiera.bs.pagoservicios.service_payment.infrastructure.constant.Constant.DATA_OK;

@Slf4j
@RestController
@RequestMapping("/recipient")
@RequiredArgsConstructor
public class RecipientV2Controller {

	private final FindLastRecipientsUseCase findLastRecipientsUseCase;
	private final RecipientRestMapper restMapper;

	@Operation(summary = "Find last recipients")
	@GetMapping("/validate")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = DATA_OK, content = @Content(schema = @Schema(implementation = V2RecipientsResponse.class))),
			@ApiResponse(responseCode = "500", description = "Error en el servidor", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
	public ResponseEntity<V2RecipientsResponse> getRecipients(
			@RequestHeader(HeaderConstant.HEADER_COD_INTERNO) String codigoInterno) {

		RecipientDashboard dashboard = findLastRecipientsUseCase.execute(codigoInterno);
		V2RecipientsResponse response = V2RecipientsResponse.builder()
				.recipients(restMapper.toResponseList(dashboard.getRecipients()))
				.synchronizeDate(dashboard.getSynchronizationDate())
				.alert(restMapper.toAlertResponse(dashboard.getAlert())).build();
		return ResponseEntity.ok(response);
	}
}
