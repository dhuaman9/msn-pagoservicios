package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class V2TransactionDto {
    private String transactionId;
    private String operationId;
    private String status;
    private String createdUser;
    private BigDecimal amount;
}
