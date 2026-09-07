package pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.adapter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.financiera.bs.pagoservicios.service_payment.domain.exception.ExternalServiceException;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Bill;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.BillList;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.BillProviderPort;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.V2GatewayInterbankRestClient;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.BillDto;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.BillResponseDto;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.external.client.domain.ClientBillDto;
import retrofit2.Response;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class BillProviderAdapter implements BillProviderPort {

	private final V2GatewayInterbankRestClient interbankRestClient;
	private static final String CURRENCY_PREFIX = "S/ ";

	@Override
	public BillList fetchBills(String recipientId, String serviceId, String clientId) {
		log.info("fetchBills recipientId={} serviceId={} clientId={}", recipientId, serviceId, clientId);

		try {
			Response<BillResponseDto> response = interbankRestClient.getBills(recipientId, serviceId, clientId)
					.execute();

			if (response.body() == null) {
				return emptyBillList();
			}

			return mapToDomain(response.body());

		} catch (IOException e) {

			throw new ExternalServiceException(String.format("External Interbank error: %s", e.getMessage()));
		}
	}

	private BillList mapToDomain(BillResponseDto body) {
		List<Bill> domainBills = Optional.ofNullable(body.getBills()).orElse(Collections.emptyList()).stream()
				.map(this::mapToBill).toList();

		return BillList.builder().client(mapToClientInfo(body.getClient())).bills(domainBills).build();
	}

	private Bill mapToBill(BillDto dto) {
		return Bill.builder().id(dto.getId()).amount(toBigDecimal(dto.getTotalAmount()))
				.amountFormat(dto.getTotalAmount() != null ? CURRENCY_PREFIX + dto.getTotalAmount() : null)
				.currency(dto.getCurrency()).discount(toBigDecimal(dto.getDiscount()))
				.commission(toBigDecimal(dto.getCommission())).dueDate(parseDate(dto.getDueDate())).build();
	}

	private BillList.ClientInfo mapToClientInfo(ClientBillDto clientDto) {
		if (clientDto == null) {
			return null;
		}
		return BillList.ClientInfo.builder().id(clientDto.getId()).name(clientDto.getName()).build();
	}

	private BigDecimal toBigDecimal(String value) {
		return value != null ? new BigDecimal(value) : null;
	}

	private LocalDate parseDate(String dateStr) {
		return dateStr != null ? LocalDate.parse(dateStr) : null;
	}

	private BillList emptyBillList() {
		return BillList.builder().bills(Collections.emptyList()).build();
	}
}
