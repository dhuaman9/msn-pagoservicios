package pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.V2Alert;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "V2RecipientsResponse")
public class V2RecipientsResponse {

    @Schema(name = "recipients")
    private List<V2RecipientResponse> recipients;

    @Schema(name = "synchronizeDate")
    private String synchronizeDate;

    @Schema(name = "alert")
    private V2Alert alert;
}
