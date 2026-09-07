package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client;

import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.ProductoRestResponse;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.ProductoRestRequest;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.ResponseListWrapper;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;


public interface ProductoRestClient {

    @POST("/bsproductos/v1/personasProd/obtenerPersonasProdPorCodInterno")
    Call<ResponseListWrapper<ProductoRestResponse>> obtenerPersonasProdPorCodInterno(@Body ProductoRestRequest request);

}
