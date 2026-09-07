package pe.financiera.bs.pagoservicios.service_payment.infrastructure.persistence.mapper;

import org.springframework.stereotype.Component;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Recipient;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.persistence.entity.RecipientJpaEntity;

@Component
public class RecipientPersistenceMapper {

    public Recipient toDomain(RecipientJpaEntity entity) {
        if (entity == null) return null;

        return Recipient.builder()
                .generatedId(entity.getGeneratedId())
                .id(entity.getId())
                .name(entity.getName())
                .supports(entity.getSupports())
                .status(entity.getStatus())
                .top(entity.getTop())
                .build();
    }

    public RecipientJpaEntity toEntity(Recipient domain) {
        if (domain == null) return null;

        return RecipientJpaEntity.builder()
                .generatedId(domain.getGeneratedId())
                .id(domain.getId())
                .name(domain.getName())
                .top(domain.getTop())
                .status(domain.getStatus())
                .supports(domain.getSupports() != null ? String.join(",", domain.getSupports()) : null)
                .build();
    }
}
