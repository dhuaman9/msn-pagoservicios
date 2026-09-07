package pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "RechargeConfirmRequest")
public class RechargeConfirmRequest {

    @Schema(name = "recipientId")
    private String recipientId;

    @Schema(name = "serviceId")
    private String serviceId;

    @Schema(name = "clientId")
    private String clientId;

    @Schema(name = "amount")
    private BigDecimal amount;
}
