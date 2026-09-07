package pe.financiera.bs.pagoservicios.service_payment.domain.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Action {
    private String text;
    private String deeplink;
}
