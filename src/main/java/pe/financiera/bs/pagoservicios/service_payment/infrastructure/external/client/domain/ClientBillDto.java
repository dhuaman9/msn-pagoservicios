package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientBillDto {
    private String id;
    private String name;
}
