package pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.response;

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
@Schema(name = "PaymentOperation")
public class PaymentOperation {

    @Schema(name = "operationId")
    private String operationId;

    @Schema(name = "operationNumber")
    private String operationNumber;

    @Schema(name = "amount")
    private BigDecimal amount;

    @Schema(name = "amountFormat")
    private String amountFormat;

    @Schema(name = "labelDetail")
    private String labelDetail;

    @Schema(name = "detail")
    private String detail;

    @Schema(name = "status")
    private String status;

    @Schema(name = "date")
    private Long date;

    @Schema(name = "name")
    private String name;

    @Schema(name = "expirationDate")
    private Long expirationDate;
}
