package pe.financiera.bs.pagoservicios.service_payment.domain.model;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class OperationResponse {
    String operationId;
    String operationNumber;
    String originAccountId;
    String destinationAccountId;
    BigDecimal amount;
    BigDecimal tipAmount;
    String type;
    String description;
    String detail;
    String additionalData;
    String operationReferenceId;
    String externalReferenceId;
    BigDecimal operationSequential;
    String originName;
    String destinationName;
    Boolean enabled;
    String status;
    String createdDate;
    String lastModifiedDate;
    String createdUser;
    String lastModifiedUser;
}
