package pe.financiera.bs.pagoservicios.service_payment.domain.model;

import lombok.Builder;
import lombok.Value;
import java.util.List;

/**
 * Agregado de Dominio que representa la vista principal de Recipientes.
 */
@Value
@Builder
public class RecipientDashboard {
    List<Recipient> recipients;
    String synchronizationDate;
    Alert alert;
}
