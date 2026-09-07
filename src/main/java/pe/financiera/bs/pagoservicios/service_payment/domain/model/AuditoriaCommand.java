package pe.financiera.bs.pagoservicios.service_payment.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
public class AuditoriaCommand {

    private String servicio;
    private String evento;
    private String codInterno;
    private String codigoOperacion;
    private String nroDocumento;
    private String usuario;
    private String otp;
    private String canal;
    private String aux1;
    private String headers;
}
