package pe.financiera.bs.pagoservicios.service_payment.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.persistence.entity.ServicePaymentJpaEntity;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.ServiceStatus;

import java.util.List;

@Repository
public interface V2ServiceRepository extends JpaRepository<ServicePaymentJpaEntity, Long> {

    @Query(
        nativeQuery = true,
        value = "select max(synchronization_id) from service where recipient_id in (select recipient_generated_id from recipient where id = ?1) group by synchronization_id order by synchronization_id desc limit 1"
    )
    Long findLastSync(String recipientId);

    List<ServicePaymentJpaEntity> findAllBySynchronizationIdAndStatusAndRecipient_Id(Long synchronizationId, ServiceStatus status, String recipientId);

    ServicePaymentJpaEntity findFirstByRecipient_IdAndIdAndStatusOrderBySynchronizationIdDesc(String recipientId, String id, ServiceStatus status);
}
