package pe.financiera.bs.pagoservicios.service_payment.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Bill;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.BillList;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.BillList.ClientInfo;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.in.GetBillsUseCase.GetBillsCommand;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.BillProviderPort;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetBillsInteractorTest {

	@Mock
	private BillProviderPort billProviderPort;

	private GetBillsInteractor interactor;
	private GetBillsCommand validCommand;
	private BillList billListMock;

	@BeforeEach
	void setUp() {
		interactor = new GetBillsInteractor(billProviderPort);

		validCommand = new GetBillsCommand("RECIPIENT-001", "SERVICE-001", "CLIENT-001");

		LocalDate dueDate = LocalDate.of(2025, Month.DECEMBER, 31);

		Bill billMock = Bill.builder().id("BILL-001").amount(BigDecimal.valueOf(100.00)).dueDate(dueDate).build();

		ClientInfo clientInfo = ClientInfo.builder().id("CLIENT-001").name("John Doe").build();

		billListMock = BillList.builder().client(clientInfo).bills(List.of(billMock)).build();
	}

	@Test
	void execute_whenValidCommand_shouldCallBillProviderPortWithCorrectParameters() {
		when(billProviderPort.fetchBills("RECIPIENT-001", "SERVICE-001", "CLIENT-001")).thenReturn(billListMock);

		BillList result = interactor.execute(validCommand);

		verify(billProviderPort).fetchBills("RECIPIENT-001", "SERVICE-001", "CLIENT-001");
		assertNotNull(result);
	}

	@Test
	void execute_whenValidCommand_shouldReturnBillList() {
		when(billProviderPort.fetchBills(anyString(), anyString(), anyString())).thenReturn(billListMock);

		BillList result = interactor.execute(validCommand);

		assertNotNull(result);
		assertNotNull(result.getBills());
	}

	@ParameterizedTest
	@ValueSource(strings = {"", "   "})
	void execute_whenRecipientIdIsEmpty_shouldThrowIllegalArgumentException(String emptyValue) {
		GetBillsCommand commandWithEmptyRecipientId = new GetBillsCommand(emptyValue, "SERVICE-001", "CLIENT-001");

		assertThrows(IllegalArgumentException.class, () -> interactor.execute(commandWithEmptyRecipientId));
	}

	@Test
	void execute_whenRecipientIdIsNull_shouldThrowIllegalArgumentException() {
		GetBillsCommand commandWithNullRecipientId = new GetBillsCommand(null, "SERVICE-001", "CLIENT-001");

		assertThrows(IllegalArgumentException.class, () -> interactor.execute(commandWithNullRecipientId));
	}

	@ParameterizedTest
	@ValueSource(strings = {"", "   "})
	void execute_whenServiceIdIsEmpty_shouldThrowIllegalArgumentException(String emptyValue) {
		GetBillsCommand commandWithEmptyServiceId = new GetBillsCommand("RECIPIENT-001", emptyValue, "CLIENT-001");

		assertThrows(IllegalArgumentException.class, () -> interactor.execute(commandWithEmptyServiceId));
	}

	@Test
	void execute_whenServiceIdIsNull_shouldThrowIllegalArgumentException() {
		GetBillsCommand commandWithNullServiceId = new GetBillsCommand("RECIPIENT-001", null, "CLIENT-001");

		assertThrows(IllegalArgumentException.class, () -> interactor.execute(commandWithNullServiceId));
	}

	@ParameterizedTest
	@ValueSource(strings = {"", "   "})
	void execute_whenClientIdIsEmpty_shouldThrowIllegalArgumentException(String emptyValue) {
		GetBillsCommand commandWithEmptyClientId = new GetBillsCommand("RECIPIENT-001", "SERVICE-001", emptyValue);

		assertThrows(IllegalArgumentException.class, () -> interactor.execute(commandWithEmptyClientId));
	}

	@Test
	void execute_whenClientIdIsNull_shouldThrowIllegalArgumentException() {
		GetBillsCommand commandWithNullClientId = new GetBillsCommand("RECIPIENT-001", "SERVICE-001", null);

		assertThrows(IllegalArgumentException.class, () -> interactor.execute(commandWithNullClientId));
	}
}
