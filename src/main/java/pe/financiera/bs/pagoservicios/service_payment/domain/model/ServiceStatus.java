package pe.financiera.bs.pagoservicios.service_payment.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ServiceStatus")
public enum ServiceStatus {
    CREATED,
    VALID
}
