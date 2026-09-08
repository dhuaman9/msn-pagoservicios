package pe.financiera.bs.pagoservicios.service_payment.domain.port.out;

import pe.financiera.bs.pagoservicios.service_payment.domain.model.PaymentResultRequest;

public interface PublishPaymentResultPort {
    void publishResult(PaymentResultRequest request);
}
