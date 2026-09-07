package pe.financiera.bs.pagoservicios.service_payment.domain.model;

import lombok.Builder;
import lombok.Value;
import java.math.BigDecimal;

@Value
@Builder
public class Transaction {
    String transactionId;
    String operationId;
    String status;
    String createdUser;
    BigDecimal amount;
}
