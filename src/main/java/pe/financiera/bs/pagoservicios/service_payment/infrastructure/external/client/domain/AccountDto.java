package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain;

import lombok.Data;

@Data
public class AccountDto {
    private String accountId;
    private String externalAccountId;
    private String externalPersonalId;
    private String sourceType;
}

