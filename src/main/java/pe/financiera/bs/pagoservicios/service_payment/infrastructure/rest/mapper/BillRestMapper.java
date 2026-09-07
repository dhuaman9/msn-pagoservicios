package pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.mapper;

import org.springframework.stereotype.Component;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Bill;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.BillList;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.response.V2BillResponse;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.rest.response.V2BillsResponse;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BillRestMapper {

    public V2BillsResponse toBillListResponse(BillList domain) {
        if (domain == null) return null;

        return V2BillsResponse.builder()
                .client(toClientResponse(domain.getClient()))
                .bills(toResponseList(domain.getBills()))
                .build();
    }

    private V2BillsResponse.ClientResponse toClientResponse(BillList.ClientInfo domain) {
        if (domain == null) return null;
        return V2BillsResponse.ClientResponse.builder()
                .id(domain.getId())
                .name(domain.getName())
                .build();
    }

    public V2BillResponse toResponse(Bill domain) {
        if (domain == null) return null;

        return V2BillResponse.builder()
                .id(domain.getId())
                .currency(domain.getCurrency())
                .totalAmount(domain.getAmount() != null ? domain.getAmount().toString() : null)
                .totalAmountFormat(domain.getAmountFormat())
                .discount(domain.getDiscount() != null ? domain.getDiscount().toString() : "0.00")
                .commission(domain.getCommission() != null ? domain.getCommission().toString() : "0.00")
                .dueDate(domain.getDueDateInMillisec())
                .build();
    }

    public List<V2BillResponse> toResponseList(List<Bill> domainList) {
        if (domainList == null) return null;
        return domainList.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
