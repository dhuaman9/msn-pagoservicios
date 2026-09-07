package pe.financiera.bs.pagoservicios.service_payment.domain.port.out;

import pe.financiera.bs.pagoservicios.service_payment.domain.model.AuditoriaCommand;

public interface AuditoriaPort {

    void procesarAuditoria(Object request, Object response, AuditoriaCommand auditoriaCommand);
}
