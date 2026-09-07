package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain;

import lombok.Data;

import java.util.List;

@Data
public class ResultAccountResponse {
    private List<AccountDto> result;
}
