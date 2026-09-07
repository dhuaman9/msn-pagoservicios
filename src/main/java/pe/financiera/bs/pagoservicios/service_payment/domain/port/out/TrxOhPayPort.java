package pe.financiera.bs.pagoservicios.service_payment.domain.port.out;

import pe.financiera.bs.pagoservicios.service_payment.domain.model.AutorizacionCommand;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.AutorizationResult;

public interface TrxOhPayPort {

    AutorizationResult autorizar(AutorizacionCommand autorizacionCommand);

    void reversar(AutorizacionCommand autorizacionCommand);
}
