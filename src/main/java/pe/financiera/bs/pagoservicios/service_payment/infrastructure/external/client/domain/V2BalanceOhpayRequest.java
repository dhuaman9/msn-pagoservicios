package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class V2BalanceOhpayRequest {
    private String accountId;
    private String externalAccountId;
    private String externalPersonalId;
    private String operationType;
    private String operationCode;
    private String groupingCode;
    private String flow;
    private BigDecimal amount;
}
