package pe.financiera.bs.pagoservicios.service_payment.infrastructure.persistence.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.SynchronizationPort;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.persistence.entity.SynchronizationJpaEntity;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.persistence.repository.V2SynchronizationRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class SynchronizationAdapter implements SynchronizationPort {

    private final V2SynchronizationRepository synchronizationRepository;

    @Override
    public SynchronizationJpaEntity getLastSynchronizationDate() {
        log.info("Fetching last synchronization date from local V2 repository");

        SynchronizationJpaEntity lastSync = synchronizationRepository.findFirstByOrderByCreatedTimeDesc();

        if (lastSync == null) {
            log.warn("No synchronization record found in repository");
            return null;
        }

        return lastSync;
    }
}
