package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@ToString
public class ResponseWrapper<T> {

    private Integer codigo;

    private String mensaje;

    private T data;

    public void setDataOK(T data) {
        this.codigo = 0;
        this.mensaje = "OK";
        this.data = data;
    }

    public void setError(String mensaje, Integer code) {
        this.codigo = code;
        this.mensaje = mensaje;
    }
}
