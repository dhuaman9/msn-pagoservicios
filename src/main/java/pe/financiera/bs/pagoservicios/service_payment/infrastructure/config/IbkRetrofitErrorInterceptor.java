package pe.financiera.bs.pagoservicios.service_payment.infrastructure.config;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatusCode;
import pe.financiera.bs.pagoservicios.service_payment.domain.exception.IbkRestClientException;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.mapper.IbkErrorCodeMapper;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.response.InterbankErrorResponse;

import java.io.IOException;

@NoArgsConstructor
public class IbkRetrofitErrorInterceptor implements Interceptor {

	@Override
	public Response intercept(Chain chain) throws IOException {
		Request request = chain.request();
		Response response = chain.proceed(request);

		if (!response.isSuccessful()) {
			try (ResponseBody errorBody = response.body()) {
				if (errorBody != null) {
					String httpBodyResponse = errorBody.string();

					InterbankErrorResponse ibkError = getResponseDto(httpBodyResponse);

					int mappedErrorCode = IbkErrorCodeMapper.mapCode(ibkError.getCode());

					throw new IbkRestClientException(HttpStatusCode.valueOf(response.code()), ibkError.getMessage(),
							ibkError.getDescription(), mappedErrorCode);
				}
			}
		}

		return response;
	}

	private InterbankErrorResponse getResponseDto(String msgError) {
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			return objectMapper.readValue(msgError, InterbankErrorResponse.class);
		} catch (IOException e) {
			InterbankErrorResponse response = new InterbankErrorResponse();
			response.setMessage(msgError);
			return response;
		}
	}
}
