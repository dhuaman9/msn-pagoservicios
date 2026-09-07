package pe.financiera.bs.pagoservicios.service_payment.domain.exception;

import java.io.Serial;

import org.springframework.http.HttpStatusCode;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class IbkRestClientException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 1L;

	private final HttpStatusCode statusCode;
	private final String message;
	private final String description;
	private final int code;

	public IbkRestClientException(HttpStatusCode statusCode, String message, String description, int code) {
		super(message);
		this.statusCode = statusCode;
		this.message = message;
		this.description = description;
		this.code = code;
	}
}
