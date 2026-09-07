package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Alert;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.AlertPort;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.V2HomeRestClientAdapter;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.V2Alert;

/**
 * Adaptador de Alertas de Home (V2).
 * Ahora utiliza el nuevo V2HomeRestClientAdapter, logrando la independencia total.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HomeAlertAdapter implements AlertPort {

    private final V2HomeRestClientAdapter homeRestClientAdapter;

    @Override
    public Alert getHomeAlert(String userId) {
        log.info("Getting home alert for userId: {}", userId);

        V2Alert alert = homeRestClientAdapter.getHomeAlert(userId);

        if (alert == null) {
            log.info("No home alert found for user");
            return null;
        }

        return Alert.builder()
                .title(alert.getTitle())
                .message(alert.getMessage())
                .type(alert.getType())
                .build();
    }
}
