package pe.financiera.bs.pagoservicios.service_payment.domain.model;

import lombok.Builder;
import lombok.Value;
import java.math.BigDecimal;

@Value
@Builder
public class PaymentRequest {
    String codInterno;
    String recipientId;
    String serviceId;
    String billId;
    String clientId;
    BigDecimal amount;
    String deviceUUID;
    OperationType operationType;
    String headersAsJson;
}
