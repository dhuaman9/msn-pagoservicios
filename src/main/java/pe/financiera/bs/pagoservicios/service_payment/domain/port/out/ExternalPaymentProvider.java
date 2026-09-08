package pe.financiera.bs.pagoservicios.service_payment.domain.port.out;

import pe.financiera.bs.pagoservicios.service_payment.domain.model.Card;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.PaymentRequest;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.ProductoResult;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.RechargeRequest;

public interface ExternalPaymentProvider {

	String processPayment(PaymentRequest paymentRequest, String operationId, String operationNumber,
			String externalAccountId, String sourceType, ProductoResult productoResult, Card card,
			String additionalData);

	void processDirectPayment(RechargeRequest request, String operationId, String operationNumber,
			String externalAccountId, String sourceType, ProductoResult productoResult, Card card,
			String additionalData);
}
