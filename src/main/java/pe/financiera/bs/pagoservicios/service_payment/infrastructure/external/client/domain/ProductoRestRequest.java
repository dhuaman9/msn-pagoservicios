package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain;

import lombok.Builder;
import lombok.Singular;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.ProductoCommand;

import java.util.List;

@Builder
public record ProductoRestRequest(@Singular("codInterno") List<String> codInterno,
		@Singular("tipoTarjeta") List<String> tipoTarjeta, @Singular("codBloqueoCuenta") List<String> codBloqueoCuenta,
		@Singular("codBloqueoTarjeta") List<String> codBloqueoTarjeta, Integer bloqueoCuentaCondicion,
		@Singular("codProducto") List<String> codProducto) {

	public static ProductoRestRequest of(ProductoCommand productoCommand) {
		return ProductoRestRequest.builder().codInterno(productoCommand.codInterno())
				.tipoTarjeta(productoCommand.tipoTarjeta()).codBloqueoCuenta(productoCommand.codBloqueoCuenta())
				.codBloqueoTarjeta(productoCommand.codBloqueoTarjeta())
				.bloqueoCuentaCondicion(productoCommand.bloqueoCuentaCondicion())
				.codProducto(productoCommand.codProducto()).build();
	}
}
