package pe.financiera.bs.pagoservicios.service_payment.domain.model;

import lombok.Builder;
import lombok.Value;

/**
 * Domain Model para Recipient.
 * 100% independiente de la infraestructura JPA antigua.
 */
@Value
@Builder(toBuilder = true)
public class Recipient {
    Long generatedId;
    String id;
    String name;
    String supports;
    RecipientStatus status;
    Boolean top;
}
