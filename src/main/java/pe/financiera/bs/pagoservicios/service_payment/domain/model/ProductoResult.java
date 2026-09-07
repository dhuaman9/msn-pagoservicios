package pe.financiera.bs.pagoservicios.service_payment.domain.model;

import java.time.LocalDateTime;

public record ProductoResult(Integer tipoDocumento, String numeroDocumento, Integer tipoProducto, String numeroCuenta,
		String bloqueoCuenta, String numeroTarjeta, String tipoTarjeta, String bloqueoTarjeta, String nombreEmbozado,
		String claseTarjeta, LocalDateTime fecRegistro, String usuRegistro, String usuActualizacion, String uidcliente,
		String uidcuenta) {
}
