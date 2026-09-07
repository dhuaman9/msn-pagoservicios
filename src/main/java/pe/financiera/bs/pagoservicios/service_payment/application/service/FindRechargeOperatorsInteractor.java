package pe.financiera.bs.pagoservicios.service_payment.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Operator;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Recipient;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.in.FindRechargeOperatorsUseCase;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.RecipientRepositoryPort;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.ServiceRepositoryPort;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FindRechargeOperatorsInteractor implements FindRechargeOperatorsUseCase {

    private static final String LOG_PREFIX = "BS_SERVICE_PAY";

    private final RecipientRepositoryPort recipientRepositoryPort;
    private final ServiceRepositoryPort serviceRepositoryPort;

    @Override
    public List<Operator> execute() {
        log.info("{}_RECHARGE_VALIDATE_STEP_1_FIND_TOP_UP_RECIPIENTS", LOG_PREFIX);
        List<Recipient> recipients = recipientRepositoryPort.findLatestRechargeRecipients();

        log.info("{}_RECHARGE_VALIDATE_STEP_2_RESOLVE_SERVICE_PER_OPERATOR: count={}", LOG_PREFIX, recipients.size());
        return recipients.stream()
                .map(this::toOperator)
                .collect(Collectors.toList());
    }

    private Operator toOperator(Recipient recipient) {
        List<pe.financiera.bs.pagoservicios.service_payment.domain.model.Service> services =
                serviceRepositoryPort.findAllByLastSyncAndRecipient(recipient.getId());
        pe.financiera.bs.pagoservicios.service_payment.domain.model.Service service =
                services != null && !services.isEmpty() ? services.get(0) : null;
        return Operator.builder()
                .id(recipient.getId())
                .name(recipient.getName())
                .service(service)
                .build();
    }
}
