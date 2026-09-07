package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client;

import com.google.gson.JsonSyntaxException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import pe.financiera.framework.common.util.RestExceptionMessages;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.V2Alert;
import retrofit2.Response;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Adaptador de Cliente REST (V2) para Home.
 * Implementación clonada para asegurar la independencia total del paquete original.
 */
@Slf4j
@Component("v2HomeRestClientAdapter") // Nombre de bean único para evitar colisiones
@RequiredArgsConstructor
public class V2HomeRestClientAdapter {

    private final V2HomeRestClient homeRestClient;

    public V2Alert getHomeAlert(String userId) {
        try {
            log.info("Getting home alert for userId {} with V2 Client", userId);

            Response<V2Alert> response = homeRestClient.getHomeAlert(true, false).execute();

            if (!response.isSuccessful()) {
                throw new RestClientException(RestExceptionMessages.UNABLE_TO_PARSE_DATA);
            }
            return response.body();

        } catch (JsonSyntaxException ex) {
            throw new RestClientException(RestExceptionMessages.UNABLE_TO_PARSE_JSON);
        } catch (UnknownHostException | SocketTimeoutException | ConnectException ex) {
            throw new RestClientException(RestExceptionMessages.UNABLE_TO_CONNECT_TO_HOST);
        } catch (IOException e) {
            throw new RestClientException(RestExceptionMessages.GENERAL_EXCEPTION);
        }
    }
}
