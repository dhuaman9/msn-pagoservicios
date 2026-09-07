package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillDto {
    private String id;
    private String currency;
    private String totalAmount;
    private String totalAmountFormat;
    private String discount;
    private String feeAmount;
    private String commission;
    private String dueDate;
}
