package pe.financiera.bs.pagoservicios.service_payment.domain.model;

import lombok.Builder;
import lombok.Singular;
import java.util.List;

@Builder
public record ProductoCommand(@Singular("codInterno") List<String> codInterno,
		@Singular("tipoTarjeta") List<String> tipoTarjeta, @Singular("codBloqueoCuenta") List<String> codBloqueoCuenta,
		@Singular("codBloqueoTarjeta") List<String> codBloqueoTarjeta, Integer bloqueoCuentaCondicion,
		@Singular("codProducto") List<String> codProducto) {
}
