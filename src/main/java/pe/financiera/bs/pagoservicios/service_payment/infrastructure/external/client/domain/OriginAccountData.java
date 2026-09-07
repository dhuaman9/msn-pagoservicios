package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain;

import lombok.Builder;

@Builder
public record OriginAccountData(String principalName, String identDocType, String identDocNumber, String accountNumber,
		String entityCode) {

}
