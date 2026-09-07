package pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class InterbankErrorResponse {

    private String message;

    private String code;

    private String description;
}
