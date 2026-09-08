package pe.financiera.bs.pagoservicios.service_payment.infrastructure.messaging.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import pe.financiera.framework.event.base.message.third.party.Header;
import pe.financiera.framework.pubsub.queue.publisher.MessagePublisher;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.PaymentResultRequest;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.PublishPaymentResultPort;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.event.Body;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.event.IncorporateEventTagEnum;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.event.TransactionEvent;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.messaging.mapper.ProcessorBillCode;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.messaging.mapper.ServicePaymentAdditionalData;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class PaymentResultAdapter implements PublishPaymentResultPort {

	private static final String SERVICE_PAYMENT_IBK_RESULT = "servicePaymentIBK";
	private static final String IBK_RESULT_SUCCESS = "success";
	private static final String IBK_RESULT_FAILED = "failed";
	private static final String BILL_TYPE_CODE = "billTypeCode";
	private static final String SERVICE_PAYMENT_TYPE = "SERVICE_PAYMENT";
	private static final String PHONE_RECHARGE_TYPE = "PHONE_RECHARGE";
	private static final String COMMERCE_TERMINAL_ID = "commerceTerminalId";
	private static final String CUSTOMER_UUID = "customerUID";
	private static final String COMMERCE_CODE = "commerceCode";
	private static final String COMMERCE_NAME = "commerceName";
	private static final String REASON = "reason";
	private static final String COMMERCE_DESCRIPTION = "commerceTrxDescription";
	private static final String BBR_PROCESSOR = "BBR_PROCESSOR";
	private static final String COMMAND_TRIGGER_RECHARGE = "RECHARGE";
	private static final String TRANSFER_TYPE_CASH_IN = "CASH_IN";

	private final MessagePublisher incorporateStartedTopic;
	private final ObjectMapper objectMapper;

	public PaymentResultAdapter(@Qualifier("incorporateStartedTopic") MessagePublisher incorporateStartedTopic,
			ObjectMapper objectMapper) {
		this.incorporateStartedTopic = incorporateStartedTopic;
		this.objectMapper = objectMapper;
	}

	@Override
	public void publishResult(PaymentResultRequest request) {
		log.info("V2 Messaging: Publishing payment result for transaction {}, success: {}", request.getTransactionId(),
				request.isSuccess());

		TransactionEvent event = new TransactionEvent();
		event.setHeader(buildHeader(request));
		event.setBody(buildBody(request));
		event.setCustomProperties(buildCustomProperties(request));

		try {
			incorporateStartedTopic.publishMessage(event);
			log.info("V2 Messaging: Payment result event published successfully");
		} catch (Exception e) {
			log.error("Error publishing payment result event", e);
		}
	}

	private Header buildHeader(PaymentResultRequest request) {
		Header header = new Header();
		header.setCommandTrigger(request.getCommandTrigger());
		header.setEventId(UUID.randomUUID().toString());
		header.setEventTag(
				request.isSuccess() ? IncorporateEventTagEnum.COMPLETED.name() : IncorporateEventTagEnum.FAILED.name());
		header.setTimestamp(Date.from(Instant.now()));
		return header;
	}

	private Body buildBody(PaymentResultRequest request) {
		Body body = new Body();
		body.setOperationId(request.getOperationId());
		body.setOperationNumber(request.getOperationNumber());
		body.setTransactionId(request.getTransactionId());
		body.setTransferAmount(request.getAmount());
		body.setUserId(request.getUserId());
		body.setTransferType(TRANSFER_TYPE_CASH_IN);
		body.setContract(request.getContract());
		body.setSourceType(request.getSourceType());
		body.setDocumentType(request.getDocumentType());
		body.setDocumentNumber(request.getDocumentNumber());
		body.setResult(request.isSuccess());
		return body;
	}

	private Map<String, Object> buildCustomProperties(PaymentResultRequest request) {
		Map<String, Object> customProperties = new HashMap<>();
		if (request.getCustomProperties() != null) {
			customProperties.putAll(request.getCustomProperties());
		}

		// Propiedades dinámicas basadas en el estado del pago
		customProperties.put(SERVICE_PAYMENT_IBK_RESULT, request.isSuccess() ? IBK_RESULT_SUCCESS : IBK_RESULT_FAILED);

		if (BBR_PROCESSOR.equals(request.getSourceType())) {
			ProcessorBillCode billCode = ProcessorBillCode.getBillCode(request.getCommandTrigger());
			customProperties.put(CUSTOMER_UUID, request.getCustomerUid());
			customProperties.put(BILL_TYPE_CODE,
					COMMAND_TRIGGER_RECHARGE.equals(request.getCommandTrigger())
							? PHONE_RECHARGE_TYPE
							: SERVICE_PAYMENT_TYPE);
			customProperties.put(COMMERCE_TERMINAL_ID, billCode.getCommerceTerminalId());
			customProperties.put(COMMERCE_CODE, resolveRecipientId(request.getAdditionalData()));
			customProperties.put(COMMERCE_NAME, request.getCommerceName());
			customProperties.put(REASON, billCode.getCommerceName());
			customProperties.put(COMMERCE_DESCRIPTION, billCode.getCommerceName());
		}
		return customProperties;
	}

	private String resolveRecipientId(String additionalData) {
		if (additionalData == null || additionalData.isBlank()) {
			return null;
		}
		try {
			return objectMapper.readValue(additionalData, ServicePaymentAdditionalData.class).getRecipientId();
		} catch (Exception e) {
			log.error("Error parsing additionalData for recipientId", e);
			return null;
		}
	}
}
