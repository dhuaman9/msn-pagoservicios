package pe.financiera.bs.pagoservicios.service_payment.domain.model;

public enum OperationType {
    SERVICE_PAYMENT,
    RECHARGE;

    public static OperationType fromString(String value) {
        if (value == null) {
            return null;
        }
        try {
            return OperationType.valueOf(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
