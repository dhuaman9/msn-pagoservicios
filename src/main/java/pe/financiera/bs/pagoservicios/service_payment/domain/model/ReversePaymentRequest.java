package pe.financiera.bs.pagoservicios.service_payment.domain.model;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;
import java.util.Map;

@Value
@Builder
public class ReversePaymentRequest {
    String operationId;
    String operationNumber;
    String transactionId;
    String userId;
    BigDecimal amount;
    String commandTrigger;
    String contract;
    String sourceType;
    String customerUid;
    String documentType;
    String documentNumber;
    String cardToken;
    String commerceName;
    String additionalData;
    Map<String, Object> customProperties;
}
