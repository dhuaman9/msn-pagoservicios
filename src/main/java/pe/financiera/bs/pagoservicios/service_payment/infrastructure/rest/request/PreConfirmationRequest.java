package pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "PreConfirmationRequest")
public class PreConfirmationRequest {

    @Schema(description = "recipientId")
    private String recipientId;

    @Schema(description = "serviceId")
    private String serviceId;

    @Schema(description = "clientId")
    private String clientId;
}
