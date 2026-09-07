package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class V2TransactionUpdateDto {
    private String status;
    private String lastModifiedUser;
}
