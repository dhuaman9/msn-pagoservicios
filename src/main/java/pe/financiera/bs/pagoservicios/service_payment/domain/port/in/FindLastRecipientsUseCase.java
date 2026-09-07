package pe.financiera.bs.pagoservicios.service_payment.domain.port.in;

import pe.financiera.bs.pagoservicios.service_payment.domain.model.RecipientDashboard;

/**
 * Puerto de Entrada para buscar el Dashboard de Recipientes.
 */
public interface FindLastRecipientsUseCase {

    RecipientDashboard execute(String userId);

}
