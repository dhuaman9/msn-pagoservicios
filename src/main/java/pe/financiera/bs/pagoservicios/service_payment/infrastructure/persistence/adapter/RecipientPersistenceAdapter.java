package pe.financiera.bs.pagoservicios.service_payment.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Recipient;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.RecipientStatus;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.RecipientType;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.RecipientRepositoryPort;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.persistence.entity.RecipientJpaEntity;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.persistence.entity.SynchronizationJpaEntity;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.persistence.mapper.RecipientPersistenceMapper;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.persistence.repository.V2RecipientRepository;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RecipientPersistenceAdapter implements RecipientRepositoryPort {

    private final V2RecipientRepository recipientRepository;
    private final RecipientPersistenceMapper mapper;

    @Override
    public List<Recipient> findLatestRecipients(SynchronizationJpaEntity syncEntity) {
        log.info("Fetching latest recipients from repository");
        int statusOrdinal = RecipientStatus.VALID.ordinal();

        Integer synchronizationId = syncEntity != null && syncEntity.getId() != null ? syncEntity.getId().intValue() : null;

        List<RecipientJpaEntity> latestRecipients;

        if (synchronizationId != null) {
            latestRecipients = recipientRepository.findLatestRecipients(RecipientType.PAYMENT.getType(), statusOrdinal, synchronizationId);
        }  else {
            latestRecipients = recipientRepository.findLatestRecipients(RecipientType.PAYMENT.getType(), statusOrdinal);
        }

        return latestRecipients
                .stream()
                .map(mapper::toDomain)
                .toList();
    }

	@Override
	public Recipient findLatestRecipientById(String recipientId) {
        log.info("findLatestRecipientById: {}", recipientId);

		RecipientJpaEntity entity = recipientRepository.findLatestRecipientById(recipientId,
				RecipientStatus.VALID.ordinal());
		return entity != null ? mapper.toDomain(entity) : null;
	}

    @Override
    public List<Recipient> findLatestRechargeRecipients() {
        List<RecipientJpaEntity> entities = recipientRepository.findLatestRecipients(
            RecipientType.RECHARGE.getType(), RecipientStatus.VALID.ordinal());
        return entities.stream()
            .map(mapper::toDomain)
            .toList();
    }
}
