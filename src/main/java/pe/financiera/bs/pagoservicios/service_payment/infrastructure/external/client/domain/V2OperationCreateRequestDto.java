package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class V2OperationCreateRequestDto {
    private String originAccountId;
    private String destinationAccountId;
    private BigDecimal amount;
    private BigDecimal tipAmount;
    private String type;
    private String description;
    private String detail;
    private String additionalData;
    private String operationReferenceId;
    private String externalReferenceId;
    private String originName;
    private String destinationName;
    private String status;
    private String createdUser;
    private Boolean enabled;
}
