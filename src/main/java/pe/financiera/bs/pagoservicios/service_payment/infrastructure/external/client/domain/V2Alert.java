package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "V2Alert")
public class V2Alert {

    @Schema(name = "title")
    private String title;

    @Schema(name = "message")
    private String message;

    @Schema(name = "type")
    private String type;

    @Schema(name = "screen")
    private String screen;

    @Schema(name = "action")
    private Action action;

}
