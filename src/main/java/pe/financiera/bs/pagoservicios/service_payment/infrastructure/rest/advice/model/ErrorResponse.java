package pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.advice.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@AllArgsConstructor
@Schema(name = "ErrorResponse")
public class ErrorResponse {

    @Schema(name = "code", example = "340")
    private int code;

    @Schema(name = "message")
    private String message;


}
