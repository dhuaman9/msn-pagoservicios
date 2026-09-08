package pe.financiera.bs.pagoservicios.service_payment.domain.model;

import java.math.BigDecimal;
import java.util.Map;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class PaymentResultRequest {
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
    String commerceName;
    String additionalData;
    boolean success;
    Map<String, Object> customProperties;


}
