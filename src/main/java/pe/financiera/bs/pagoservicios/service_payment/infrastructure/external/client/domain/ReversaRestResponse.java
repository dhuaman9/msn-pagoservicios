package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain;

public record ReversaRestResponse(String transactionId, String authorizationCode, boolean success) {

}
