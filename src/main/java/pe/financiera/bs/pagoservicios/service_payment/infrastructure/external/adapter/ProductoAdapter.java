package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import pe.financiera.bs.pagoservicios.service_payment.domain.exception.ExternalServiceException;
import pe.financiera.bs.pagoservicios.service_payment.domain.exception.ModelNotFoundException;
import pe.financiera.bs.pagoservicios.service_payment.domain.exception.ModelRestClientException;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.ProductoCommand;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.ProductoResult;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.ProductoRestClient;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.ProductoRestRequest;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.ProductoRestResponse;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.ProductoPort;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.ResponseListWrapper;
import retrofit2.Response;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductoAdapter implements ProductoPort {

	private final ProductoRestClient productoRestClient;

	@Override
	public ProductoResult buscarProducto(ProductoCommand productoCommand) {
		log.info("buscarProducto producto {}", productoCommand);

		try {
			ProductoRestRequest request = ProductoRestRequest.of(productoCommand);

			Response<ResponseListWrapper<ProductoRestResponse>> response = productoRestClient
					.obtenerPersonasProdPorCodInterno(request).execute();

			if (!response.isSuccessful()) {
				throw new ModelRestClientException(HttpStatusCode.valueOf(response.code()),
						"Error calling buscarProducto");
			}

			if (response.body() == null || response.body().getData() == null || response.body().getData().isEmpty()) {
				throw new ModelNotFoundException(
						"No se encontró producto con el código interno: " + productoCommand.codInterno());
			}

			ProductoRestResponse restResponse = response.body().getData().getFirst();

			return restResponse.toDomain();

		} catch (IOException e) {
			throw new ExternalServiceException("External service communication error: " + e.getMessage());
		}
	}

}
