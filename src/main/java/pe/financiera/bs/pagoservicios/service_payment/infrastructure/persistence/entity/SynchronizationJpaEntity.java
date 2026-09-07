package pe.financiera.bs.pagoservicios.service_payment.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "synchronization", schema = "ecatalogo")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SynchronizationJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_synchronization")
    private Long id;

    @Column(name = "created_time")
    private LocalDateTime createdTime;
}
