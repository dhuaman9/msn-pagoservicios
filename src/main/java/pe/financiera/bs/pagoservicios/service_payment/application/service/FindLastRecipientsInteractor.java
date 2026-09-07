package pe.financiera.bs.pagoservicios.service_payment.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Alert;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Recipient;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.RecipientDashboard;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.in.FindLastRecipientsUseCase;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.AlertPort;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.RecipientRepositoryPort;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.SynchronizationPort;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.persistence.entity.SynchronizationJpaEntity;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FindLastRecipientsInteractor implements FindLastRecipientsUseCase {

    private final RecipientRepositoryPort recipientRepositoryPort;
    private final SynchronizationPort synchronizationPort;
    private final AlertPort alertPort;

    private static final String LOG_PREFIX = "BS_SERVICE_PAY";

    @Override
    public RecipientDashboard execute(String userId) {
        log.info("{}_VALIDATE_STEP_3.2_GET_LAST_SYNC: userId: {}", LOG_PREFIX, userId);
        Assert.hasText(userId, "User ID must not be empty");

        SynchronizationJpaEntity syncEntity = synchronizationPort.getLastSynchronizationDate();

        log.info("{}_VALIDATE_STEP_3.3_FIND_RECIPIENTS: syncEntity: {}", LOG_PREFIX, syncEntity);
        List<Recipient> recipients = recipientRepositoryPort.findLatestRecipients(syncEntity);

        log.info("{}_VALIDATE_STEP_3.4_GET_HOME_ALERT: userId: {}", LOG_PREFIX, userId);
        Alert alert = alertPort.getHomeAlert(userId);

        log.info("{}_VALIDATE_STEP_3.5_END_COMPLETED: userId: {}", LOG_PREFIX, userId);
        return RecipientDashboard.builder()
                .recipients(recipients)
                .synchronizationDate(syncEntity != null && syncEntity.getCreatedTime() != null ? syncEntity.getCreatedTime().toString() : null)
                .alert(alert)
                .build();
    }

}
