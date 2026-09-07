package pe.financiera.bs.pagoservicios.service_payment.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.RecipientStatus;

@Entity
@Table(name = "recipient", schema = "ecatalogo")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipientJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "recipient_generated_id")
    private Long generatedId;

    private String id;
    private String name;
    private String supports; // CSV string

    @Enumerated(EnumType.ORDINAL)
    private RecipientStatus status;

    private Boolean top;

    @Column(name = "Synchronization_ID")
    private Long synchronizationId;

}
