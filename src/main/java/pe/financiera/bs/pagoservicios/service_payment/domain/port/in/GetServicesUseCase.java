package pe.financiera.bs.pagoservicios.service_payment.domain.port.in;

import pe.financiera.bs.pagoservicios.service_payment.domain.model.Service;
import java.util.List;

public interface GetServicesUseCase {
    List<Service> execute(String recipientId);
}
