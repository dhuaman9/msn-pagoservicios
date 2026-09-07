package pe.financiera.bs.pagoservicios.service_payment.domain.port.out;

import pe.financiera.bs.pagoservicios.service_payment.domain.model.Service;
import java.util.List;

public interface ServiceRepositoryPort {

    List<Service> findAllByLastSyncAndRecipient(String recipientId);

    Service findByRecipientAndServiceId(String recipientId, String serviceId);
}
