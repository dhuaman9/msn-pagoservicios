package pe.financiera.bs.pagoservicios.service_payment.domain.port.in;

import pe.financiera.bs.pagoservicios.service_payment.domain.model.Operator;

import java.util.List;

public interface FindRechargeOperatorsUseCase {
    List<Operator> execute();
}
