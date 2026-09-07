package pe.financiera.bs.pagoservicios.service_payment.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Service;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.ServiceStatus;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.ServiceRepositoryPort;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.persistence.entity.ServicePaymentJpaEntity;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.persistence.repository.V2ServiceRepository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ServicePersistenceAdapter implements ServiceRepositoryPort {

    private final V2ServiceRepository serviceRepository;

    @Override
    public List<Service> findAllByLastSyncAndRecipient(String recipientId) {
        log.info("SERVICE_PAYMENT_getServices_STEP_4: Find lastSync from repository for recipientId: {}", recipientId);
        Long lastSync = serviceRepository.findLastSync(recipientId);

        if (lastSync == null) {
            return Collections.emptyList();
        }

        List<ServicePaymentJpaEntity> entities = serviceRepository.findAllBySynchronizationIdAndStatusAndRecipient_Id(
                lastSync, ServiceStatus.VALID, recipientId);

        log.info("SERVICE_PAYMENT_getServices_STEP_5: Fetching services from repository for recipientId: {}", recipientId);

        return entities.stream()
                .map(entity -> Service.builder()
                        .id(entity.getId())
                        .name(entity.getName())
                        .type(entity.getType())
                        .label(entity.getLabel())
                        .length(entity.getLength())
                        .dataType(entity.getDataType())
                        .status(entity.getStatus())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
	public Service findByRecipientAndServiceId(String recipientId, String serviceId) {
		ServicePaymentJpaEntity entity = serviceRepository
				.findFirstByRecipient_IdAndIdAndStatusOrderBySynchronizationIdDesc(recipientId, serviceId,
						ServiceStatus.VALID);
		if (entity == null) {
			return null;
		}
		return Service.builder().id(entity.getId()).name(entity.getName()).type(entity.getType())
				.label(entity.getLabel()).length(entity.getLength()).dataType(entity.getDataType())
				.status(entity.getStatus()).build();
	}
}
