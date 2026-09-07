package pe.financiera.bs.pagoservicios.service_payment.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Card {
    String cardId;
    String token;
    String status;
}
