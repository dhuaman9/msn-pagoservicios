package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain;

import lombok.Data;

import java.util.List;

@Data
public class V2ResultTransactionResponseDto {
    private List<V2TransactionDto> result;
}
