package pe.financiera.bs.pagoservicios.service_payment.domain.model;

import lombok.Builder;
import lombok.Value;
import java.util.List;

@Value
@Builder
public class BillList {
    ClientInfo client;
    List<Bill> bills;

    @Value
    @Builder
    public static class ClientInfo {
        String id;
        String name;
    }
}
