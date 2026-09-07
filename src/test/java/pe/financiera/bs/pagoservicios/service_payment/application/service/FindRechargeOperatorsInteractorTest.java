package pe.financiera.bs.pagoservicios.service_payment.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Operator;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Recipient;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.RecipientStatus;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Service;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.ServiceStatus;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.RecipientRepositoryPort;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.ServiceRepositoryPort;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindRechargeOperatorsInteractorTest {

    private FindRechargeOperatorsInteractor interactor;

    private RecipientRepositoryPort recipientRepositoryPort;
    private ServiceRepositoryPort serviceRepositoryPort;

    private Recipient recipientOneMock;
    private Recipient recipientTwoMock;
    private Service serviceMock;

    @BeforeEach
    void setUp() {
        recipientRepositoryPort = mock(RecipientRepositoryPort.class);
        serviceRepositoryPort = mock(ServiceRepositoryPort.class);
        interactor = new FindRechargeOperatorsInteractor(recipientRepositoryPort, serviceRepositoryPort);

        recipientOneMock = Recipient.builder()
                .generatedId(1L)
                .id("REC-001")
                .name("Claro")
                .supports("RECHARGE")
                .status(RecipientStatus.VALID)
                .top(true)
                .build();

        recipientTwoMock = Recipient.builder()
                .generatedId(2L)
                .id("REC-002")
                .name("Movistar")
                .supports("RECHARGE")
                .status(RecipientStatus.VALID)
                .top(false)
                .build();

        serviceMock = Service.builder()
                .id("SVC-001")
                .name("Recarga Claro")
                .type("RECHARGE")
                .label("Número de teléfono")
                .length(9)
                .dataType("NUMERIC")
                .status(ServiceStatus.VALID)
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Lista vacía de recipientes
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void execute_whenNoRechargeRecipients_shouldReturnEmptyList() {
        when(recipientRepositoryPort.findLatestRechargeRecipients()).thenReturn(Collections.emptyList());

        List<Operator> result = interactor.execute();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(serviceRepositoryPort, never()).findAllByLastSyncAndRecipient(org.mockito.ArgumentMatchers.anyString());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Flujo exitoso: recipientes con servicios disponibles
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void execute_whenRecipientsExist_shouldReturnMappedOperators() {
        when(recipientRepositoryPort.findLatestRechargeRecipients()).thenReturn(List.of(recipientOneMock, recipientTwoMock));
        when(serviceRepositoryPort.findAllByLastSyncAndRecipient("REC-001")).thenReturn(List.of(serviceMock));
        when(serviceRepositoryPort.findAllByLastSyncAndRecipient("REC-002")).thenReturn(List.of(serviceMock));

        List<Operator> result = interactor.execute();

        assertEquals(2, result.size());

        Operator firstOperator = result.get(0);
        assertEquals("REC-001", firstOperator.getId());
        assertEquals("Claro", firstOperator.getName());
        assertEquals(serviceMock, firstOperator.getService());

        Operator secondOperator = result.get(1);
        assertEquals("REC-002", secondOperator.getId());
        assertEquals("Movistar", secondOperator.getName());
        assertEquals(serviceMock, secondOperator.getService());
    }

    @Test
    void execute_whenRecipientsExist_shouldCallFindAllByLastSyncPerRecipient() {
        when(recipientRepositoryPort.findLatestRechargeRecipients()).thenReturn(List.of(recipientOneMock, recipientTwoMock));
        when(serviceRepositoryPort.findAllByLastSyncAndRecipient("REC-001")).thenReturn(List.of(serviceMock));
        when(serviceRepositoryPort.findAllByLastSyncAndRecipient("REC-002")).thenReturn(List.of(serviceMock));

        interactor.execute();

        verify(serviceRepositoryPort).findAllByLastSyncAndRecipient("REC-001");
        verify(serviceRepositoryPort).findAllByLastSyncAndRecipient("REC-002");
    }

    @Test
    void execute_whenMultipleServicesForRecipient_shouldTakeFirstService() {
        Service secondService = Service.builder()
                .id("SVC-002")
                .name("Otro servicio")
                .type("RECHARGE")
                .status(ServiceStatus.VALID)
                .build();

        when(recipientRepositoryPort.findLatestRechargeRecipients()).thenReturn(List.of(recipientOneMock));
        when(serviceRepositoryPort.findAllByLastSyncAndRecipient("REC-001"))
                .thenReturn(List.of(serviceMock, secondService));

        List<Operator> result = interactor.execute();

        assertEquals(1, result.size());
        assertEquals(serviceMock, result.get(0).getService());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Recipiente con servicios nulos o vacíos → service = null
    // ─────────────────────────────────────────────────────────────────────────

    @ParameterizedTest
    @MethodSource("provideEmptyOrNullServiceLists")
    void execute_whenServicesNullOrEmpty_shouldSetServiceAsNull(List<Service> serviceList) {
        when(recipientRepositoryPort.findLatestRechargeRecipients()).thenReturn(List.of(recipientOneMock));
        when(serviceRepositoryPort.findAllByLastSyncAndRecipient("REC-001")).thenReturn(serviceList);

        List<Operator> result = interactor.execute();

        assertEquals(1, result.size());
        assertNull(result.get(0).getService());
    }

    private static Stream<Arguments> provideEmptyOrNullServiceLists() {
        List<Service> nullList = null;
        List<Service> emptyList = Collections.emptyList();

        return Stream.of(
                Arguments.of(nullList),
                Arguments.of(emptyList)
        );
    }
}

