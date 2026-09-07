package pe.financiera.bs.pagoservicios.service_payment.infrastructure.messaging.mapper;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ServicePaymentAdditionalData {
    public static final String HOLDER = "Titular";
    public static final String SERVICE = "Servicio";
    public static final String DUE_DATE = "Fecha de vencimiento";

    private String holderLabel;
    private String holderValue;
    private String serviceLabel;
    private String serviceValue;
    private String supplyNumberLabel;
    private String supplyNumberValue;
    private String dueDateLabel;
    private Long dueDateValue;
    private String recipientId;
    private String serviceId;
}
