package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client;

import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.ResultAccountResponse;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.ResultCardResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface V2AccountRestClient {
    @GET("/accounts")
    Call<ResultAccountResponse> getAccountBySourceType(@Query("userId") String userId, @Query("sourceType") String sourceType, @Query("enabled") Boolean aTrue);

    @GET("/cards")
    Call<ResultCardResponse> getCardByAccountId(@Query("accountId") String accountId);
}
