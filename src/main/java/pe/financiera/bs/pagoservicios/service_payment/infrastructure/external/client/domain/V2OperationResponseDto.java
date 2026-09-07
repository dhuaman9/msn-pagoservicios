package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class V2OperationResponseDto {
    private String operationId;
    private String operationNumber;
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
    private BigDecimal operationSequential;
    private String originName;
    private String destinationName;
    private Boolean enabled;
    private String status;
    private String createdDate;
    private String lastModifiedDate;
    private String createdUser;
    private String lastModifiedUser;
}
