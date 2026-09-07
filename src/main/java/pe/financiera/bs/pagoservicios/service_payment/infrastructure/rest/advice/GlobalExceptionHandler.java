package pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pe.financiera.bs.pagoservicios.service_payment.domain.exception.ExternalServiceException;
import pe.financiera.bs.pagoservicios.service_payment.domain.exception.IbkRestClientException;
import pe.financiera.bs.pagoservicios.service_payment.domain.exception.ModelNotFoundException;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.advice.model.ErrorResponse;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	private static final int DEFAULT_ERROR_CODE = -1;

	@ExceptionHandler(ModelNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleProductNotFound(ModelNotFoundException ex) {
		log.warn("Recurso no encontrado: {}", ex.getMessage());
		ErrorResponse error = new ErrorResponse(DEFAULT_ERROR_CODE, ex.getMessage());
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
	}

	@ExceptionHandler(ExternalServiceException.class)
	public ResponseEntity<ErrorResponse> handleExternalServiceException(ExternalServiceException ex) {
		log.error("Falla de infraestructura externa: {}", ex.getMessage(), ex);
		ErrorResponse error = new ErrorResponse(DEFAULT_ERROR_CODE,
				"El servicio externo no está disponible temporalmente.");
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(error);
	}

	@ExceptionHandler(IbkRestClientException.class)
	public ResponseEntity<ErrorResponse> handleExternalServiceException(IbkRestClientException ex) {
		log.error("Error ibk: {}", ex.getMessage(), ex);
		ErrorResponse error = new ErrorResponse(ex.getCode(),ex.getMessage());
		return ResponseEntity.status(ex.getStatusCode()).body(error);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
		log.error("Error interno no controlado: {}", ex.getMessage(), ex);
		ErrorResponse error = new ErrorResponse(DEFAULT_ERROR_CODE, "Ocurrió un error inesperado.");
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
	}
}
