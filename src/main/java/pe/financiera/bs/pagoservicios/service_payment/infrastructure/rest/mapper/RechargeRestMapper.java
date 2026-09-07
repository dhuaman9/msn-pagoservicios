package pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.mapper;

import org.springframework.stereotype.Component;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Operator;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Service;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.response.OperatorListResponse;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.response.OperatorResponse;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.response.OperatorServiceResponse;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class RechargeRestMapper {

    public OperatorListResponse toOperatorListResponse(List<Operator> operators) {
        if (operators == null) {
            return OperatorListResponse.builder().operators(List.of()).build();
        }
        return OperatorListResponse.builder()
                .operators(operators.stream().map(this::toOperatorResponse).collect(Collectors.toList()))
                .build();
    }

    public OperatorResponse toOperatorResponse(Operator operator) {
        if (operator == null) return null;
        return OperatorResponse.builder()
                .id(operator.getId())
                .name(operator.getName())
                .service(toServiceResponse(operator.getService()))
                .build();
    }

    public OperatorServiceResponse toServiceResponse(Service service) {
        if (service == null) return null;
        return OperatorServiceResponse.builder()
                .id(service.getId())
                .name(service.getName())
                .serviceType(service.getType())
                .label(service.getLabel())
                .length(service.getLength())
                .dataType(service.getDataType())
                .build();
    }
}
