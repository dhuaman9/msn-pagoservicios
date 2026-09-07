package pe.financiera.bs.pagoservicios.service_payment.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.BillList;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.in.GetBillsUseCase;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.BillProviderPort;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetBillsInteractor implements GetBillsUseCase {

	private final BillProviderPort billProviderPort;

	@Override
	public BillList execute(GetBillsCommand command) {
		log.info("execute command={}", command);

		validate(command);

		BillList billList = billProviderPort.fetchBills(command.recipientId(), command.serviceId(), command.clientId());

		log.info("execute count={}", billList.getBills().size());

		return billList;
	}

	private void validate(GetBillsCommand command) {
		Assert.hasText(command.recipientId(), "Recipient ID is required");
		Assert.hasText(command.serviceId(), "Service ID is required");
		Assert.hasText(command.clientId(), "Client ID is required");
	}
}
