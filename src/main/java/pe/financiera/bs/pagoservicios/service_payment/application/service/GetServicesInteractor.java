package pe.financiera.bs.pagoservicios.service_payment.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import pe.financiera.bs.pagoservicios.service_payment.domain.exception.ModelNotFoundException;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Service;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.in.GetServicesUseCase;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.ServiceRepositoryPort;

import java.util.List;

@Slf4j
@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class GetServicesInteractor implements GetServicesUseCase {

	private final ServiceRepositoryPort serviceRepositoryPort;

	@Override
	public List<Service> execute(String recipientId) {
		log.info("execute: {}", recipientId);

		List<Service> services = serviceRepositoryPort.findAllByLastSyncAndRecipient(recipientId);

		if (CollectionUtils.isEmpty(services)) {
			throw new ModelNotFoundException("Services not found for recipientId: " + recipientId);
		}

		return services;
	}
}
