package pe.financiera.bs.pagoservicios.service_payment.domain.port.out;

import pe.financiera.bs.pagoservicios.service_payment.domain.model.Alert;

/**
 * Puerto de Salida para obtener alertas del sistema.
 */
public interface AlertPort {
    Alert getHomeAlert(String userId);
}
