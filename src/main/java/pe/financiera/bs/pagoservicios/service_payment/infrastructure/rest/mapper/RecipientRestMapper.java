package pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.mapper;

import org.springframework.stereotype.Component;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.Action;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Alert;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Recipient;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.V2Alert;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.response.V2RecipientResponse;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RecipientRestMapper {

    public V2RecipientResponse toResponse(Recipient domain) {
        if (domain == null) return null;

        return V2RecipientResponse.builder()
                .id(domain.getId())
            .generatedId(domain.getGeneratedId())
                .name(domain.getName())
                .supports(domain.getSupports())
                .top(domain.getTop())
                .status(domain.getStatus().name())
                .build();
    }

    public List<V2RecipientResponse> toResponseList(List<Recipient> domainList) {
        if (domainList == null) return null;
        return domainList.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public V2Alert toAlertResponse(Alert domain) {
        if (domain == null) return null;

        return V2Alert.builder()
                .title(domain.getTitle())
                .message(domain.getMessage())
                .type(domain.getType())
                .screen(domain.getScreen())
                .action(toActionResponse(domain.getAction()))
                .build();
    }

    public Action toActionResponse(pe.financiera.bs.pagoservicios.service_payment.domain.model.Action domain) {
        if (domain == null) return null;

        return Action.builder()
                .text(domain.getText())
                .deeplink(domain.getDeeplink())
                .build();
    }
}
