package pe.financiera.bs.pagoservicios.service_payment.domain.port.out;

import pe.financiera.bs.pagoservicios.service_payment.domain.model.BillList;

/**
 * Puerto de Salida para obtener facturas de proveedores externos.
 */
public interface BillProviderPort {
    BillList fetchBills(String recipientId, String serviceId, String clientId);
}
