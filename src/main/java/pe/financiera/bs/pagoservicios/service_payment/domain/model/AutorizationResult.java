package pe.financiera.bs.pagoservicios.service_payment.domain.model;

public record AutorizationResult(String transactionId, String authorizationCode, boolean success) {

}
