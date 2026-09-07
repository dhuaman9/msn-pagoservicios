package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillResponseDto {
    private ClientBillDto client;
    private List<BillDto> bills;
}
