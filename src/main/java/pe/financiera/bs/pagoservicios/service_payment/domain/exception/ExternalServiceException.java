package pe.financiera.bs.pagoservicios.service_payment.domain.exception;

import java.io.Serial;

public class ExternalServiceException extends RuntimeException{

    @Serial
    private static final long serialVersionUID = 1L;

    public ExternalServiceException(String mensaje) {
        super(mensaje);
    }
}
