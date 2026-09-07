package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ResponseListWrapper<T>{

	private Integer codigo;

	private String mensaje;

	private List<T> data;



}
