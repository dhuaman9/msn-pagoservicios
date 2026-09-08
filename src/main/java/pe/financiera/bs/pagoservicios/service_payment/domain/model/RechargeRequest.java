package pe.financiera.bs.pagoservicios.service_payment.domain.model;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class RechargeRequest {

    String codInterno;
    String recipientId;
    String serviceId;
    String clientId;
    BigDecimal amount;
    String deviceUUID;
    OperationType operationType;
    String headersAsJson;
}
