package pe.financiera.bs.pagoservicios.service_payment.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Service;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.messaging.mapper.ServicePaymentAdditionalData;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.AutorizacionCommand;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.AutorizationResult;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Bill;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.BillList;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Card;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.OperationResponse;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.OperationType;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.PaymentExecutionResult;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.PaymentRequest;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.ProductoCommand;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.ProductoResult;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Recipient;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.in.GetBillsUseCase;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.in.PayServiceUseCase;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.ExternalPaymentProvider;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.OperationPort;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.ProductoPort;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.RecipientRepositoryPort;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.ServiceRepositoryPort;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.TrxOhPayPort;

import java.text.DecimalFormat;

import static pe.financiera.bs.pagoservicios.service_payment.application.service.constant.ServicesConstant.BLOQ_N;
import static pe.financiera.bs.pagoservicios.service_payment.application.service.constant.ServicesConstant.DEB_FISICO;
import static pe.financiera.bs.pagoservicios.service_payment.application.service.constant.ServicesConstant.DEB_VIRTUAL;
import static pe.financiera.bs.pagoservicios.service_payment.application.service.constant.ServicesConstant.PRINCIPAL;

@Slf4j
@RequiredArgsConstructor
public class PayServiceInteractor implements PayServiceUseCase {
	private static final String DEFAULT_VALUE = "-";
	private static final DecimalFormat PRICE_FORMATTER = new DecimalFormat("S/ ###,##0.00");

	private final ProductoPort productoPort;
	private final TrxOhPayPort trxOhPayPort;
	private final OperationPort operationPort;
	private final ExternalPaymentProvider externalPaymentProvider;
	private final GetBillsUseCase getBillsUseCase;
	private final ServiceRepositoryPort serviceRepositoryPort;
	private final RecipientRepositoryPort recipientRepositoryPort;
	private final ObjectMapper objectMapper;

	@Override
	public PaymentExecutionResult execute(PaymentRequest request) {
		log.info("execute: request={}", request);

		ProductoCommand productoCommand = ProductoCommand.builder().codInterno(request.getCodInterno())
				.tipoTarjeta(PRINCIPAL).codBloqueoCuenta(BLOQ_N).codBloqueoTarjeta(BLOQ_N).bloqueoCuentaCondicion(1)
				.codProducto(DEB_FISICO).codProducto(DEB_VIRTUAL).build();

		ProductoResult productoResult = productoPort.buscarProducto(productoCommand);

		//TODO
		Card card = null;

		Service service = serviceRepositoryPort
				.findByRecipientAndServiceId(request.getRecipientId(), request.getServiceId());

		Recipient recipient = recipientRepositoryPort.findLatestRecipientById(request.getRecipientId());

		Bill bill = resolveBill(request);

		String additionalData = buildAdditionalData(service, productoResult.nombreEmbozado(), bill, request);

		OperationResponse createdOperation = operationPort.createOperation(request.getCodInterno(),
				productoResult.numeroCuenta(), request.getAmount(), "SERVICE_PAYMENT", request.getRecipientId(),
				productoResult.nombreEmbozado(), recipient != null ? recipient.getName() : DEFAULT_VALUE,
				"Payment service", request.getClientId(), additionalData);

		String operationId = createdOperation.getOperationId();

		var bbrTransaction = operationPort.createTransaction(operationId, request.getCodInterno(), "CASH_OUT",
				"BBR_PROCESSOR", request.getAmount(), createdOperation.getOperationNumber());

		AutorizacionCommand autorizacionCommand = AutorizacionCommand.builder().customerUid(productoResult.uidcliente())
				.accountUid(productoResult.uidcuenta()).operationNumber(createdOperation.getOperationNumber())
				.amount(request.getAmount()).customerDocumentType(String.valueOf(productoResult.tipoDocumento()))
				.customerDocumentNumber(productoResult.numeroDocumento())
				.customerFullName(productoResult.nombreEmbozado()).operationType(OperationType.RECHARGE).build();

		AutorizationResult autorizationResult = trxOhPayPort.autorizar(autorizacionCommand);

		operationPort.updateTransactionStatus(bbrTransaction.getTransactionId(), "COMPLETED", request.getCodInterno());

		var ibkTransaction = operationPort.createTransaction(operationId, request.getCodInterno(), "CASH_OUT", "IBK",
				request.getAmount(), createdOperation.getOperationNumber());

		//TODO ver sourceType
		externalPaymentProvider.processPayment(request, operationId, createdOperation.getOperationNumber(),
				productoResult.uidcuenta(), null, productoResult, card, additionalData);

		operationPort.updateTransactionStatus(ibkTransaction.getTransactionId(), "WAITTING_FOR_IBK",
				request.getCodInterno());

		return PaymentExecutionResult.builder().operationId(operationId)
				.operationNumber(createdOperation.getOperationNumber())
				.amount(createdOperation.getAmount() != null ? createdOperation.getAmount() : request.getAmount())
				.amountFormat(PRICE_FORMATTER.format(request.getAmount()))
				.labelDetail(service != null && service.getLabel() != null ? service.getLabel() : DEFAULT_VALUE)
				.detail(createdOperation.getDescription() != null
						? createdOperation.getDescription()
						: "Payment service")
				.status(createdOperation.getStatus() != null ? createdOperation.getStatus() : "PENDING")
				.authorizationCode(autorizationResult.authorizationCode()).message("Payment registered")
				.date(parseEpochSeconds())
				.name(recipient != null && recipient.getName() != null ? recipient.getName() : DEFAULT_VALUE)
				.expirationDate(bill != null ? bill.getDueDateInMillisec() : null).build();

	}

	private Bill resolveBill(PaymentRequest request) {
		BillList billList = getBillsUseCase.execute(GetBillsUseCase.GetBillsCommand.of(request.getRecipientId(),
				request.getServiceId(), request.getClientId()));
		return billList.getBills().stream().filter(b -> request.getBillId().equals(b.getId())).findFirst()
				.orElseThrow();
	}

	private String buildAdditionalData(Service service,
                                       String nomEmbozado, Bill bill, PaymentRequest request) {
		var additionalData = ServicePaymentAdditionalData
				.builder()
				.holderLabel(
						ServicePaymentAdditionalData.HOLDER)
				.holderValue(nomEmbozado)
				.serviceLabel(
						ServicePaymentAdditionalData.SERVICE)
				.serviceValue(service != null ? service.getName() : null)
				.supplyNumberLabel(service != null ? service.getLabel() : null).supplyNumberValue(request.getClientId())
				.dueDateLabel(
						ServicePaymentAdditionalData.DUE_DATE)
				.dueDateValue(bill != null ? bill.getDueDateInMillisec() : null).recipientId(request.getRecipientId())
				.serviceId(request.getServiceId()).build();
		try {
			return objectMapper.writeValueAsString(additionalData);
		} catch (JsonProcessingException e) {
			log.warn("Could not serialize operation additionalData: {}", e.getMessage(), e);
			return null;
		}
	}

	private Long parseEpochSeconds() {
		return System.currentTimeMillis() / 1000;
	}
}
