package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Builder
public record AutorizationRestRequest(

    String customerUID,
    String accountUID,
    String messageType,
    String operationCode,
    String groupingCode,
    String entryCode,
    String externalReference,
    BigDecimal amount,
    String commerceCode,
    String commerceName,
    String commerceTerminalId,
    LocalDateTime commerceDateTime,
    Integer commerceTransactionNumber,
    String commerceTrxDescription,
    OriginAccountData originAccountData,
    Boolean notificationOverride,
    String usuario,
    String canal
) {}
