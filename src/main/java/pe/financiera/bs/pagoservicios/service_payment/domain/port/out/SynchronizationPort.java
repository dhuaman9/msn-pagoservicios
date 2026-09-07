package pe.financiera.bs.pagoservicios.service_payment.domain.port.out;

import pe.financiera.bs.pagoservicios.service_payment.infrastructure.persistence.entity.SynchronizationJpaEntity;

/**
 * Puerto de Salida para obtener información de sincronización.
 */
public interface SynchronizationPort {
    SynchronizationJpaEntity getLastSynchronizationDate();
}
