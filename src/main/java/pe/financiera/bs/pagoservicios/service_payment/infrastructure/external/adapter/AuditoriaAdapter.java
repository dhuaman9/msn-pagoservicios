package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.AuditoriaCommand;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.AuditoriaPort;
import pe.financieraoh.framework.auditoria.producer.message.AuditoriaProducerService;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.AuditoriaRequest;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuditoriaAdapter implements AuditoriaPort {

	private static final String DOMINIO = "Pago de Servicios";
	private static final String FLUJO_NEGOCI0 = "bspagoservicios";
	private static final String CAPA = "bspagoservicios";

	private final ObjectMapper objectMapper;

	private final AuditoriaProducerService auditoriaProducerService;

	@Async
	@Override
	public void procesarAuditoria(Object request, Object response,
			AuditoriaCommand auditoriaCommand) {
		log.info("procesarAuditoria request: {}, response: {}, auditoriaDto: {}", request, response, auditoriaCommand);

		AuditoriaRequest auditoriaRequest = new AuditoriaRequest();
		auditoriaRequest.setBodyRequest(objectToJson(request));
		auditoriaRequest.setBodyResponse(objectToJson(response));
		auditoriaRequest.setDominio(DOMINIO);
		auditoriaRequest.setFlujoNegocio(FLUJO_NEGOCI0);
		auditoriaRequest.setServicio(auditoriaCommand.getServicio());
		auditoriaRequest.setEvento(auditoriaCommand.getEvento());
		auditoriaRequest.setCapa(CAPA);
		auditoriaRequest.setCanal(auditoriaCommand.getCanal());
		auditoriaRequest.setCodigoInterno(auditoriaCommand.getCodInterno());
		auditoriaRequest.setCodigoOtp(auditoriaCommand.getOtp());
		auditoriaRequest.setCodigoOperacion(auditoriaCommand.getCodigoOperacion());
		auditoriaRequest.setNumeroDocumento(auditoriaCommand.getNroDocumento());
		auditoriaRequest.setUsuario(auditoriaCommand.getUsuario());
		auditoriaRequest.setAux1(auditoriaCommand.getAux1());
		auditoriaRequest.setHeaderRequest(auditoriaCommand.getHeaders());

		registrarAuditoria(auditoriaRequest);
	}

	private void registrarAuditoria(AuditoriaRequest request) {
		log.info("registrarAuditoria request: {}", request);

		try {
			String jsonRequest = objectMapper.writeValueAsString(request);

			auditoriaProducerService.publicarAuditoria(jsonRequest);

		} catch (JsonProcessingException e) {
			log.error("Error registro Auditoria request: {}, error:{}", request, e.getMessage());
		}

	}

	private String objectToJson(Object object) {
		ObjectMapper mapper = new ObjectMapper();
		String json = null;
		try {
			if (object != null)
				json = mapper.writeValueAsString(object);

		} catch (JsonProcessingException e) {
			log.error("Error al convertir objecto a json: %s", e);
		}
		return json;
	}
}
