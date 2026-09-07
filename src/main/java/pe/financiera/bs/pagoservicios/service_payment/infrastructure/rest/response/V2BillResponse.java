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
@Schema(description = "V2BillResponse")
public class V2BillResponse {

    @Schema(description = "id")
    private String id;

    @Schema(description = "currency")
    private String currency;

    @Schema(description = "totalAmount")
    private String totalAmount;

    @Schema(description = "totalAmountFormat")
    private String totalAmountFormat;

    @Schema(description = "discount")
    private String discount;

    @Schema(description = "commission")
    private String commission;

    @Schema(description = "dueDate")
    private Long dueDate;
}
