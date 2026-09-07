package pe.financiera.bs.pagoservicios.service_payment.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
@Schema(name = "Service")
public class Service {

    @Schema(name = "id")
    String id;

    @Schema(name = "name")
    String name;

    @Schema(name = "type")
    String type;

    @Schema(name = "label")
    String label;

    @Schema(name = "length")
    Integer length;

    @Schema(name = "dataType")
    String dataType;

    @Schema(name = "status")
    ServiceStatus status;
}
