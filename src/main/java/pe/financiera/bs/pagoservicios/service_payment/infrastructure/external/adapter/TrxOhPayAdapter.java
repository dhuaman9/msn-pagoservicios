package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.adapter;

import java.io.IOException;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pe.financiera.bs.pagoservicios.service_payment.domain.exception.ExternalServiceException;
import pe.financiera.bs.pagoservicios.service_payment.domain.exception.ModelRestClientException;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.AutorizacionCommand;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.AutorizationResult;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.OperationType;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.TrxOhPayPort;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.TrxOhPayRestClient;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.AutorizationRestRequest;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.AutorizationRestResponse;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.OriginAccountData;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.ReversaRestResponse;
import retrofit2.Response;

import static pe.financiera.bs.pagoservicios.service_payment.infrastructure.constant.ServicePaymentConstant.OPERATION_CODE_CASH_OUT;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrxOhPayAdapter implements TrxOhPayPort {

	private final TrxOhPayRestClient trxOhPayRestClient;

    private final Clock clock;

	@Override
	public AutorizationResult autorizar(AutorizacionCommand autorizacionCommand) {

		AutorizationRestRequest request = crearAutorizationRestRequest(autorizacionCommand);

		try {
			Response<AutorizationRestResponse> response = trxOhPayRestClient.autorizar(request).execute();

			if (!response.isSuccessful()) {
				throw new ModelRestClientException(HttpStatusCode.valueOf(response.code()),
						"Error calling buscarProducto: HTTP status ");
			}

			if (response.body() == null) {
				throw new ExternalServiceException("External BBR error: response body is null");
			}

			AutorizationRestResponse autorizationRestResponse = response.body();

			return autorizationRestResponse.toDomain();

		} catch (IOException e) {
			throw new ExternalServiceException("External BBR error " + e.getMessage());
		}
	}

	@Override
	public void reversar(AutorizacionCommand autorizacionCommand) {


        AutorizationRestRequest request = crearAutorizationRestRequest(autorizacionCommand);

		try {
			Response<ReversaRestResponse> response = trxOhPayRestClient.reversar(request).execute();

			if (!response.isSuccessful()) {
				throw new ModelRestClientException(HttpStatusCode.valueOf(response.code()),
						"Error calling buscarProducto: HTTP status ");
			}

		} catch (IOException e) {
			throw new ExternalServiceException("External BBR error " + e.getMessage());
		}
	}

	private AutorizationRestRequest crearAutorizationRestRequest(AutorizacionCommand autorizacionCommand) {

		boolean isRecharge = autorizacionCommand.operationType() == OperationType.RECHARGE;

		String groupingCode = isRecharge ? "17" : "16";
		String commerceLabel = isRecharge ? "Recarga de Celular" : "Pago de servicio";
		String commerceName = autorizacionCommand.recipientName() != null
				? autorizacionCommand.recipientName()
				: commerceLabel;

		String commerceTerminalId = isRecharge ? "5" : "4";

		LocalDateTime fechaActual = LocalDateTime.now(clock);

		OriginAccountData originAccountData = OriginAccountData.builder()
				.principalName(autorizacionCommand.customerFullName())
				.identDocType(autorizacionCommand.customerDocumentType())
				.identDocNumber(autorizacionCommand.customerDocumentNumber())
				.accountNumber(autorizacionCommand.accountUid()).entityCode("03").build();


        //ver si existe campo customStr2 si existe agegar con reason sino eliminar reason
		return AutorizationRestRequest.builder().customerUID(autorizacionCommand.customerUid())
				.accountUID(autorizacionCommand.accountUid()).messageType("1100")
				.operationCode(OPERATION_CODE_CASH_OUT).groupingCode(groupingCode).entryCode("03")
				.externalReference(autorizacionCommand.operationNumber())
				.amount(autorizacionCommand.amount()).commerceCode("9999000001").commerceName(commerceName)
				.commerceTerminalId(commerceTerminalId).commerceDateTime(fechaActual)
				.commerceTransactionNumber(Math.toIntExact(fechaActual.toEpochSecond(ZoneOffset.UTC)))
				.commerceTrxDescription(commerceName).originAccountData(originAccountData).notificationOverride(false)
				.usuario(autorizacionCommand.canal()).canal(autorizacionCommand.canal()).build();
	}


}
