package pe.financiera.bs.pagoservicios.service_payment.domain.exception;

import java.io.Serial;

public class ModelNotFoundException extends RuntimeException{

    @Serial
    private static final long serialVersionUID = 1L;

    public ModelNotFoundException(String mensaje) {
        super(mensaje);
    }
}
