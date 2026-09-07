package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ReversaRestRequest {

    private String customerUID;
    private String accountUID;
    private String messageType;
    private String operationCode;
    private String groupingCode;
    private String entryCode;
    private String externalReference;
    private BigDecimal amount;
    private String commerceCode;
    private String commerceName;
    private String commerceTerminalId;
    private LocalDateTime commerceDateTime;
    private Integer commerceTransactionNumber;
    private String commerceTrxDescription;

    private Integer customInt1;
    private Integer customInt2;
    private String customStr1;
    private String customStr2;
    private BigDecimal customDec1;
    private BigDecimal customDec2;

    private OriginAccountData originAccountData;
    private Object destinationAccountData;
    private Object commissionOverride;

    private Boolean notificationOverride;
    private String usuario;
    private String canal;
    private BigDecimal itf;
}
