package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain;

import lombok.Data;

@Data
public class CardDto {
    private String cardId;
    private String token;
    private String status;
}

