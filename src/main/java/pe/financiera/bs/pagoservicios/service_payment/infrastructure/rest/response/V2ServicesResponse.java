package pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Service;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "V2ServicesResponse")
public class V2ServicesResponse {

    @Schema(name = "services")
    private List<Service> services;
}
