package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class AuditoriaRequest {

    private String aux1;

    private String aux2;

    private String aux3;

	private String codigoInterno;

	private String numeroDocumento;

	private String dominio;

	private String flujoNegocio;

	private String servicio;

	private String evento;

	private String capa;

	private String canal;

	private String sistemaOperativo;

	private String headerRequest;

	private String headerResponse;

	private String bodyRequest;

	private String bodyResponse;

	private String codigoOtp;

	private String codigoOperacion;

	private String estadoResponse;

	private String usuario;

	private String aux4;
}
