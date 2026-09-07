package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client;

import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.BillResponseDto;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.V2PaymentRequestDto;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface V2GatewayInterbankRestClient {

    @GET("/gwpagoservicios/payment/recipient/{recipientId}/service/{serviceId}/bills")
    Call<BillResponseDto> getBills(@Path("recipientId") String recipientId,
                                   @Path("serviceId") String serviceId,
                                   @Query("clientId") String clientId);

    @POST("/gwpagoservicios/payment/v2/billing")
    Call<Void> makePayment(@Body V2PaymentRequestDto request);
}
