package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class V2TransactionCreateRequestDto {
    private String operationId;
    private String type;
    private String platform;
    private String status;
    private String createdUser;
    private Boolean enabled;
    private String suppliedId;
    private String externalId;
    private BigDecimal amount;
}
