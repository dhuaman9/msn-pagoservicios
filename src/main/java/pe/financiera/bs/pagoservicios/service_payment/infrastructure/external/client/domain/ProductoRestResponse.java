package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.ProductoResult;

import java.time.LocalDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductoRestResponse(Integer tipoDocumento, String numeroDocumento, Integer tipoProducto,
		String numeroCuenta, String bloqueoCuenta, String numeroTarjeta, String tipoTarjeta, String bloqueoTarjeta,
		String nombreEmbozado, String claseTarjeta,
		@JsonDeserialize(using = LocalDateTimeDeserializer.class) @JsonSerialize(using = LocalDateTimeSerializer.class) LocalDateTime fecRegistro,
		String usuRegistro, String usuActualizacion, String uidcliente, String uidcuenta) {

	public ProductoResult toDomain() {
		return new ProductoResult(this.tipoDocumento(), this.numeroDocumento(), this.tipoProducto(),
				this.numeroCuenta(), this.bloqueoCuenta(), this.numeroTarjeta(), this.tipoTarjeta(),
				this.bloqueoTarjeta(), this.nombreEmbozado(), this.claseTarjeta(), this.fecRegistro(),
				this.usuRegistro(), this.usuActualizacion(), this.uidcliente(), this.uidcuenta());
	}
}
