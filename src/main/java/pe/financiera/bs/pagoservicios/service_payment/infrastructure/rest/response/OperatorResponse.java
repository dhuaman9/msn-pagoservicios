package pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "OperatorResponse")
public class OperatorResponse {

    @Schema(name = "id")
    private String id;

    @Schema(name = "name")
    private String name;

    @Schema(name = "service")
    private OperatorServiceResponse service;
}
