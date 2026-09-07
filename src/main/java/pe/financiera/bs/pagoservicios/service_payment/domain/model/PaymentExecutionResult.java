package pe.financiera.bs.pagoservicios.service_payment.domain.model;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class PaymentExecutionResult {
    String operationId;
    String operationNumber;
    BigDecimal amount;
    String amountFormat;
    String labelDetail;
    String detail;
    String status;
    Long date;
    String name;
    Long expirationDate;
    String authorizationCode;
    String message;
}
