package pe.financiera.bs.pagoservicios.config.interceptor;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.MDC;

import java.io.IOException;

/**
 * OkHttp interceptor que propaga el transactionId del MDC como header
 * X-Transaction-Id en las llamadas HTTP salientes, permitiendo la
 * trazabilidad cross-service de los logs por steps.
 *
 * No altera el flujo funcional: si el MDC no tiene transactionId simplemente
 * no agrega el header.
 */
public class MdcPropagationInterceptor implements Interceptor {

    public static final String TRANSACTION_ID_HEADER = "X-Transaction-Id";
    public static final String TRANSACTION_ID_MDC_KEY = "transactionId";

    @Override
    public Response intercept(Chain chain) throws IOException {
        String transactionId = MDC.get(TRANSACTION_ID_MDC_KEY);
        Request original = chain.request();

        if (transactionId != null && !transactionId.isBlank()) {
            Request withHeader = original.newBuilder()
                    .header(TRANSACTION_ID_HEADER, transactionId)
                    .build();
            return chain.proceed(withHeader);
        }

        return chain.proceed(original);
    }
}

