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
@Schema(name = "OperatorServiceResponse")
public class OperatorServiceResponse {

    @Schema(name = "id")
    private String id;

    @Schema(name = "name")
    private String name;

    @Schema(name = "serviceType")
    private String serviceType;

    @Schema(name = "label")
    private String label;

    @Schema(name = "length")
    private Integer length;

    @Schema(name = "dataType")
    private String dataType;
}
