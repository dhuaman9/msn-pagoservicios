package pe.financiera.bs.pagoservicios.service_payment.infrastructure.messaging.mapper;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum ProcessorBillCode {

    DEFAULT("", "", "", "", "", "", ""),

    CELLPHONE_RECHARGE("RECHARGE", "00", "17", "03",
        "9999000001", "Recarga de Celular", "5"),

    SERVICE_PAYMENT("SERVICE_PAYMENT", "00", "16", "03",
        "9999000001", "Pago de servicio", "4");

    private final String commandTrigger;
    private final String operationCode;
    private final String groupingCode;
    private final String entryCode;
    private final String commerceCode;
    private final String commerceName;
    private final String commerceTerminalId;

    ProcessorBillCode(String commandTrigger, String operationCode, String groupingCode, String entryCode,
                      String commerceCode, String commerceName, String commerceTerminalId) {
        this.commandTrigger = commandTrigger;
        this.operationCode = operationCode;
        this.groupingCode = groupingCode;
        this.entryCode = entryCode;
        this.commerceCode = commerceCode;
        this.commerceName = commerceName;
        this.commerceTerminalId = commerceTerminalId;
    }

    public static ProcessorBillCode getBillCode(String commandTrigger) {
        return Arrays.stream(values())
            .filter(processorBillCode -> processorBillCode.commandTrigger.equals(commandTrigger))
            .findFirst()
            .orElse(DEFAULT);
    }
}
