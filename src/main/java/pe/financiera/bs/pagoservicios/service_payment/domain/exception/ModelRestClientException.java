package pe.financiera.bs.pagoservicios.service_payment.domain.exception;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.HttpStatusCode;

import java.io.Serial;

@Getter
@Setter
@ToString
public class ModelRestClientException extends RuntimeException {

	@Serial
    private static final long serialVersionUID = 1L;

	private final HttpStatusCode statusCode;
	private final String error;

	public ModelRestClientException(HttpStatusCode statusCode, String error) {
		super(error);
		this.statusCode = statusCode;
		this.error = error;
	}
}
