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
@Schema(name = "V2RecipientResponse")
public class V2RecipientResponse {

    @Schema(name = "id")
    private String id;

    @Schema(name = "generatedId")
    private Long generatedId;

    @Schema(name = "name")
    private String name;

    @Schema(name = "supports")
    private String supports;

    @Schema(name = "top")
    private Boolean top;

    @Schema(name = "status")
    private String status;
}
