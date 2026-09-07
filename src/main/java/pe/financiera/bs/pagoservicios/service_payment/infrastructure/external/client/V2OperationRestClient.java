package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client;

import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.V2OperationCreateRequestDto;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.V2OperationUpdateRequestDto;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.V2ResultTransactionResponseDto;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.V2OperationResponseDto;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.V2TransactionCreateRequestDto;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.V2TransactionDto;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.V2TransactionUpdateDto;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface V2OperationRestClient {

    @POST("/operations")
    Call<V2OperationResponseDto> createOperation(@Body V2OperationCreateRequestDto request);

    @POST("/transactions")
    Call<V2TransactionDto> createTransaction(@Body V2TransactionCreateRequestDto request);

    @GET("/transactions")
    Call<V2ResultTransactionResponseDto> searchTransactionByFilter(@Query("search") String search);

    @GET("/operations/{operationId}")
    Call<V2OperationResponseDto> getOperation(@Path("operationId") String operationId);

    @PUT("/transactions/{transactionId}")
    Call<V2TransactionDto> updateTransaction(@Path("transactionId") String transactionId, @Body V2TransactionUpdateDto request);

    @PUT("/operations/{operationId}")
    Call<V2OperationResponseDto> updateOperation(@Path("operationId") String operationId, @Body V2OperationUpdateRequestDto request);
}
