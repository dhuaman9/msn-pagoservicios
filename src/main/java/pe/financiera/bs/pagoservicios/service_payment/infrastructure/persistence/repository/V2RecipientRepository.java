package pe.financiera.bs.pagoservicios.service_payment.infrastructure.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.persistence.entity.RecipientJpaEntity;

import java.util.List;

@Repository
public interface V2RecipientRepository extends JpaRepository<RecipientJpaEntity, Long> {

    @Query(
        nativeQuery = true,
        value = "select * from recipient where synchronization_id = (select id_synchronization from synchronization order by created_time desc limit 1) and supports = :recipientType and status = :status"
    )
    List<RecipientJpaEntity> findLatestRecipients(@Param("recipientType") String recipientType, int status);


    @Query(
        nativeQuery = true,
        value = "select * from recipient where synchronization_id = :synchronizationId and supports = :recipientType and status = :status"
    )
    List<RecipientJpaEntity> findLatestRecipients(@Param("recipientType") String recipientType, int status, int synchronizationId);

    @Query(
        nativeQuery = true,
        value = "select * from recipient where id = :recipientId and status = :status order by synchronization_id desc limit 1"
    )
    RecipientJpaEntity findLatestRecipientById(@Param("recipientId") String recipientId, @Param("status") int status);
}
