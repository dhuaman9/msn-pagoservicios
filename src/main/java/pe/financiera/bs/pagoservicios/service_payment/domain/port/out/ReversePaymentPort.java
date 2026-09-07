package pe.financiera.bs.pagoservicios.service_payment.domain.port.out;

import pe.financiera.bs.pagoservicios.service_payment.domain.model.ReversePaymentRequest;

/**
 * Puerto de Salida para iniciar el proceso de reverso de un pago fallido.
 */
public interface ReversePaymentPort {
    void triggerReverse(ReversePaymentRequest request);
}
