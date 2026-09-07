package pe.financiera.bs.pagoservicios.service_payment.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.AutorizacionCommand;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Service;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.AutorizationResult;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Card;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.OperationResponse;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.OperationType;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.PaymentExecutionResult;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.PaymentRequest;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.ProductoCommand;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.ProductoResult;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Recipient;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.in.PayRechargeUseCase;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.ExternalPaymentProvider;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.OperationPort;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.ProductoPort;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.RecipientRepositoryPort;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.ServiceRepositoryPort;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.TrxOhPayPort;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.messaging.mapper.ServicePaymentAdditionalData;

import java.text.DecimalFormat;

import static pe.financiera.bs.pagoservicios.service_payment.application.service.constant.ServicesConstant.BLOQ_N;
import static pe.financiera.bs.pagoservicios.service_payment.application.service.constant.ServicesConstant.DEB_FISICO;
import static pe.financiera.bs.pagoservicios.service_payment.application.service.constant.ServicesConstant.DEB_VIRTUAL;
import static pe.financiera.bs.pagoservicios.service_payment.application.service.constant.ServicesConstant.PRINCIPAL;

@Slf4j
@RequiredArgsConstructor
public class PayRechargeInteractor implements PayRechargeUseCase {
	private static final String DEFAULT_VALUE = "-";
	private static final DecimalFormat PRICE_FORMATTER = new DecimalFormat("S/ ###,##0.00");
	private static final String DETAIL_DEFAULT = "Recarga de Celular";

    private final ProductoPort productoPort;
    private final TrxOhPayPort trxOhPayPort;
    private final OperationPort operationPort;
    private final ExternalPaymentProvider externalPaymentProvider;
    private final ServiceRepositoryPort serviceRepositoryPort;
    private final RecipientRepositoryPort recipientRepositoryPort;
    private final ObjectMapper objectMapper;

	@Override
	public PaymentExecutionResult execute(PaymentRequest request) {
        log.info("execute: {}", request);

		PaymentRequest enrichedRequest = ensureOperationType(request);


        ProductoCommand productoCommand = ProductoCommand.builder().codInterno(request.getCodInterno())
            .tipoTarjeta(PRINCIPAL).codBloqueoCuenta(BLOQ_N).codBloqueoTarjeta(BLOQ_N).bloqueoCuentaCondicion(1)
            .codProducto(DEB_FISICO).codProducto(DEB_VIRTUAL).build();

		ProductoResult productoResult = productoPort.buscarProducto(productoCommand);
		//TODO
		Card card = null;

		Service service = serviceRepositoryPort
				.findByRecipientAndServiceId(enrichedRequest.getRecipientId(), enrichedRequest.getServiceId());

		Recipient recipient = recipientRepositoryPort.findLatestRecipientById(enrichedRequest.getRecipientId());

		String additionalData = buildAdditionalData(service, productoResult.nombreEmbozado(), enrichedRequest);

		OperationResponse createdOperation = operationPort.createOperation(enrichedRequest.getCodInterno(),
				productoResult.numeroCuenta(), enrichedRequest.getAmount(), OperationType.RECHARGE.name(),
				enrichedRequest.getRecipientId(), productoResult.nombreEmbozado(),
				recipient != null ? recipient.getName() : DEFAULT_VALUE, DETAIL_DEFAULT, enrichedRequest.getClientId(),
				additionalData);

		String operationId = createdOperation.getOperationId();

		var bbrTransaction = operationPort.createTransaction(operationId, enrichedRequest.getCodInterno(), "CASH_OUT",
				"BBR_PROCESSOR", enrichedRequest.getAmount(), createdOperation.getOperationNumber());

		AutorizacionCommand autorizacionCommand = AutorizacionCommand.builder()
				.customerUid(productoResult.uidcliente()).accountUid(productoResult.uidcuenta())
				.operationNumber(operationId).amount(request.getAmount())
				.customerDocumentType(String.valueOf(productoResult.tipoDocumento()))
				.customerDocumentNumber(productoResult.numeroDocumento())
				.customerFullName(productoResult.nombreEmbozado()).operationType(OperationType.RECHARGE)
				.build();

		AutorizationResult autorizationResult = trxOhPayPort.autorizar(autorizacionCommand);

		operationPort.updateTransactionStatus(bbrTransaction.getTransactionId(), "COMPLETED",
				enrichedRequest.getCodInterno());

		var ibkTransaction = operationPort.createTransaction(operationId, enrichedRequest.getCodInterno(), "CASH_OUT",
				"IBK", enrichedRequest.getAmount(), createdOperation.getOperationNumber());

		//TODO ver sourceType
		externalPaymentProvider.processPayment(enrichedRequest, operationId, createdOperation.getOperationNumber(),
				productoResult.uidcuenta(), null, productoResult, card, additionalData);

		operationPort.updateTransactionStatus(ibkTransaction.getTransactionId(), "WAITTING_FOR_IBK",
				enrichedRequest.getCodInterno());

		return PaymentExecutionResult.builder().operationId(operationId)
				.operationNumber(createdOperation.getOperationNumber())
				.amount(createdOperation.getAmount() != null
						? createdOperation.getAmount()
						: enrichedRequest.getAmount())
				.amountFormat(PRICE_FORMATTER.format(enrichedRequest.getAmount()))
				.labelDetail(service != null && service.getLabel() != null ? service.getLabel() : DEFAULT_VALUE)
				.detail(createdOperation.getDescription() != null ? createdOperation.getDescription() : DETAIL_DEFAULT)
				.status(createdOperation.getStatus() != null ? createdOperation.getStatus() : "PENDING")
				.authorizationCode(autorizationResult.authorizationCode()).message("Recharge registered")
				.date(System.currentTimeMillis() / 1000)
				.name(recipient != null && recipient.getName() != null ? recipient.getName() : DEFAULT_VALUE).build();

	}

	private PaymentRequest ensureOperationType(PaymentRequest request) {
		if (request.getOperationType() == OperationType.RECHARGE) {
			return request;
		}
		return PaymentRequest.builder().codInterno(request.getCodInterno()).recipientId(request.getRecipientId())
				.serviceId(request.getServiceId()).billId(request.getBillId()).clientId(request.getClientId())
				.amount(request.getAmount()).deviceUUID(request.getDeviceUUID()).operationType(OperationType.RECHARGE)
				.build();
	}

	private String buildAdditionalData(Service service,
                                       String nomEmbozado, PaymentRequest request) {

		var additionalData = ServicePaymentAdditionalData.builder().holderLabel(ServicePaymentAdditionalData.HOLDER)
				.holderValue(nomEmbozado).serviceLabel(ServicePaymentAdditionalData.SERVICE)
				.serviceValue(service != null ? service.getName() : null)
				.supplyNumberLabel(service != null ? service.getLabel() : null).supplyNumberValue(request.getClientId())
				.recipientId(request.getRecipientId()).serviceId(request.getServiceId()).build();
		try {
			return objectMapper.writeValueAsString(additionalData);
		} catch (JsonProcessingException e) {
			log.warn("Could not serialize recharge additionalData", e);
			return null;
		}
	}
}
