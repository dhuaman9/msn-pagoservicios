package pe.financiera.bs.pagoservicios.service_payment.domain.port.out;

import pe.financiera.bs.pagoservicios.service_payment.domain.model.Recipient;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.persistence.entity.SynchronizationJpaEntity;

import java.util.List;

/**
 * Puerto de Salida para la persistencia de Recipientes.
 * Define las operaciones que el dominio necesita realizar hacia el exterior.
 */
public interface RecipientRepositoryPort {
    /**
     * Recupera los últimos recipientes actualizados.
     */
    List<Recipient> findLatestRecipients(SynchronizationJpaEntity id);

    Recipient findLatestRecipientById(String recipientId);

    List<Recipient> findLatestRechargeRecipients();
}
