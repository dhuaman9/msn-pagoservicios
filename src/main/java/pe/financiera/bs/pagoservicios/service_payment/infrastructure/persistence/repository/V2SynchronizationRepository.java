package pe.financiera.bs.pagoservicios.service_payment.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.persistence.entity.SynchronizationJpaEntity;

@Repository
public interface V2SynchronizationRepository extends JpaRepository<SynchronizationJpaEntity, Long> {

    SynchronizationJpaEntity findFirstByOrderByCreatedTimeDesc();
}
