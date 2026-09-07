package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client;

import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.V2Alert;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

import static pe.financiera.bs.pagoservicios.service_payment.infrastructure.constant.HeaderConstant.*;

/**
 * Cliente Retrofit (V2) para el microservicio de Home.
 * Clonado para independizar el paquete service_payment.
 */
public interface V2HomeRestClient {

    @GET("/home/message")
    Call<V2Alert> getHomeAlert(@Header(X_VALIDATE_INTERNET) Boolean validateInternet,
                               @Header(X_VALIDATE_EMAIL) Boolean validateEmail);

}
