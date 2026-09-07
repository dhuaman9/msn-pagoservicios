package pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "V2BillsResponse")
public class V2BillsResponse {

    @Schema(description = "client")
    private ClientResponse client;

    @Schema(description = "bills")
    private List<V2BillResponse> bills;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ClientResponse {

        @Schema(description = "id")
        private String id;

        @Schema(description = "name")
        private String name;
    }
}
