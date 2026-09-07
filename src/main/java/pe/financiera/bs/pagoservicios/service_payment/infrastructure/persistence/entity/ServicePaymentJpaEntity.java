package pe.financiera.bs.pagoservicios.service_payment.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.ServiceStatus;

@Entity
@Table(name = "service", schema = "ecatalogo")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicePaymentJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_generated_id")
    private Long generatedId;

    private String id;
    private String name;
    private String type;
    private String label;
    private Integer length;
    private String dataType;

    @Enumerated(EnumType.ORDINAL)
    private ServiceStatus status;

    @ManyToOne(optional = false)
    @JoinColumn(name = "synchronization_id")
    private SynchronizationJpaEntity synchronization;

    @ManyToOne(optional = false)
    @JoinColumn(name = "recipient_id")
    private RecipientJpaEntity recipient;
}
