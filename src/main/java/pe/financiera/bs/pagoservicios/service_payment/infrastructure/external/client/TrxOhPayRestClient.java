package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client;

import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.AutorizationRestRequest;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.AutorizationRestResponse;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.ReversaRestResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface TrxOhPayRestClient {

    @POST("/bstrxohpay/v1/autorizar/ohpay")
    Call<AutorizationRestResponse> autorizar(@Body AutorizationRestRequest request);

    @POST("/bstrxohpay/v1/reversar/ohpay")
    Call<ReversaRestResponse> reversar(@Body AutorizationRestRequest request);

}
