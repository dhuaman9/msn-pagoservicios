package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain;


import pe.financiera.bs.pagoservicios.service_payment.domain.model.AutorizationResult;

public record AutorizationRestResponse(String transactionId, String authorizationCode, boolean success) {

public AutorizationResult toDomain() {
        return new AutorizationResult(this.transactionId, this.authorizationCode, this.success);
    }
}
