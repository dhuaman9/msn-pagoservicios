package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.financiera.bs.pagoservicios.service_payment.domain.exception.ExternalServiceException;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Card;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.PaymentRequest;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.ProductoResult;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.RechargeRequest;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.ExternalPaymentProvider;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.V2GatewayInterbankRestClient;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.DirectPaymentRequestDto;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.V2PaymentRequestDto;

import java.io.IOException;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalPaymentAdapter implements ExternalPaymentProvider {

	private final V2GatewayInterbankRestClient interbankRestClient;

	private final Clock clock;

	@Override
	public String processPayment(PaymentRequest paymentRequest, String operationId, String operationNumber,
			String externalAccountId, String sourceType, ProductoResult productoResult, Card card,
			String additionalData) {

		log.info("processPayment operationId={} billId={}", operationId, paymentRequest.getBillId());

		Map<String, Object> customProperties = new HashMap<>();
		customProperties.put("recipientId", paymentRequest.getRecipientId());
		customProperties.put("serviceId", paymentRequest.getServiceId());
		customProperties.put("billId", paymentRequest.getBillId());
		customProperties.put("clientId", paymentRequest.getClientId());
		customProperties.put("deviceUUID", paymentRequest.getDeviceUUID());
		customProperties.put("additionalData", additionalData);

		V2PaymentRequestDto requestDto = V2PaymentRequestDto.builder().recipientId(paymentRequest.getRecipientId())
				.serviceId(paymentRequest.getServiceId()).billId(paymentRequest.getBillId())
				.correlationId(operationNumber).clientId(paymentRequest.getClientId()).operationId(operationId)
				.operationNumber(operationNumber).contract(externalAccountId).currencyType("PEN")
				.transferAmount(paymentRequest.getAmount().toPlainString()).amount(paymentRequest.getAmount())
				.transferType("CASH_OUT").sourceType(sourceType).userId(paymentRequest.getCodInterno())
				.documentType(String.valueOf(productoResult.tipoDocumento()))
				.documentNumber(productoResult.numeroDocumento()).creationDate(OffsetDateTime.now(clock).toString())
				.tokenTunki(card != null ? card.getToken() : null).referenceNumber(operationNumber)
				.externalAccountId(externalAccountId).commandTrigger("SERVICE_PAYMENT").eventTag("STARTED")
				.customProperties(customProperties).build();

		try {
			interbankRestClient.makePayment(requestDto).execute();
			return "SUCCESS";
		} catch (IOException e) {
			throw new ExternalServiceException(
					String.format("External Interbank connectivity error: %s", e.getMessage()));
		}
	}

	@Override
	public void processDirectPayment(RechargeRequest request, String operationId, String operationNumber,
			String externalAccountId, String sourceType, ProductoResult productoResult, Card card,
			String additionalData) {
		log.info("processDirectPayment operationId={}", operationId);

		Map<String, Object> customProperties = new HashMap<>();
		customProperties.put("recipientId", request.getRecipientId());
		customProperties.put("serviceId", request.getServiceId());
		customProperties.put("clientId", request.getClientId());
		customProperties.put("deviceUUID", request.getDeviceUUID());
		customProperties.put("additionalData", additionalData);

		DirectPaymentRequestDto requestDto = DirectPaymentRequestDto.builder().recipientId(request.getRecipientId())
				.serviceId(request.getServiceId()).correlationId(operationNumber).clientId(request.getClientId())
				.operationId(operationId).operationNumber(operationNumber).contract(externalAccountId)
				.currencyType("PEN").transferAmount(request.getAmount().toPlainString()).amount(request.getAmount())
				.transferType("CASH_OUT").sourceType(sourceType).userId(request.getCodInterno())
				.documentType(String.valueOf(productoResult.tipoDocumento()))
				.documentNumber(productoResult.numeroDocumento()).creationDate(OffsetDateTime.now(clock).toString())
				.tokenTunki(card != null ? card.getToken() : null).referenceNumber(operationNumber)
				.externalAccountId(externalAccountId).commandTrigger("RECHARGE").eventTag("STARTED")
				.customProperties(customProperties).build();

		try {

			interbankRestClient.makeDirectPayment(requestDto).execute();

		} catch (IOException e) {
			throw new ExternalServiceException(
					String.format("External Interbank connectivity error: %s", e.getMessage()));
		}
	}
}
