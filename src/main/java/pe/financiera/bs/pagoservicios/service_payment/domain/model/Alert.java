package pe.financiera.bs.pagoservicios.service_payment.domain.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Alert {
    String title;
    String message;
    String type;
    String screen;
    Action action;
}
