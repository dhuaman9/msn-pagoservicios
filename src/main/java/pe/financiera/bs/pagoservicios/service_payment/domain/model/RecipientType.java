package pe.financiera.bs.pagoservicios.service_payment.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RecipientType {
    RECHARGE("TOP_UP"),
    PAYMENT("BILLS");

    private String type;

    RecipientType(String type) {
        this.type = type;
    }

}
