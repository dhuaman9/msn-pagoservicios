package pe.financiera.bs.pagoservicios.service_payment.domain.port.in;

import pe.financiera.bs.pagoservicios.service_payment.domain.model.BillList;

public interface GetBillsUseCase {
	BillList execute(GetBillsCommand command);

	record GetBillsCommand(String recipientId, String serviceId, String clientId) {
		public static GetBillsCommand of(String recipientId, String serviceId, String clientId) {
			return new GetBillsCommand(recipientId, serviceId, clientId);
		}
	}
}
