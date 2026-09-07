package pe.financiera.bs.pagoservicios.service_payment.domain.port.in;

import pe.financiera.bs.pagoservicios.service_payment.domain.model.PaymentExecutionResult;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.PaymentRequest;

public interface PayRechargeUseCase {
    PaymentExecutionResult execute(PaymentRequest request);
}
