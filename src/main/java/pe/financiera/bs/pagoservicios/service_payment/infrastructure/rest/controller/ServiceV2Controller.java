package pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Service;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.in.GetServicesUseCase;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.advice.model.ErrorResponse;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.response.V2ServicesResponse;
import java.util.List;

import static pe.financiera.bs.pagoservicios.service_payment.infrastructure.constant.Constant.DATA_OK;
import static pe.financiera.bs.pagoservicios.service_payment.infrastructure.constant.HeaderConstant.RECIPIENT_ID;

@Slf4j
@RestController
@RequestMapping("/service")
@RequiredArgsConstructor
public class ServiceV2Controller {

	private final GetServicesUseCase getServicesUseCase;

	@Operation(summary = "get services")
	@GetMapping
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = DATA_OK, content = @Content(schema = @Schema(implementation = V2ServicesResponse.class))),
			@ApiResponse(responseCode = "404", description = "Not Found", content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "500", description = "Error en el servidor", content = @Content(schema = @Schema(implementation = ErrorResponse.class)))})
	public ResponseEntity<V2ServicesResponse> getServices(
			@RequestParam(value = RECIPIENT_ID) final String recipientId) {
		List<Service> services = getServicesUseCase.execute(recipientId);
		V2ServicesResponse response = V2ServicesResponse.builder().services(services).build();
		return ResponseEntity.ok(response);
	}
}
