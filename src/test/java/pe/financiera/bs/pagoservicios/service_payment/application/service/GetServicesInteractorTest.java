package pe.financiera.bs.pagoservicios.service_payment.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.financiera.bs.pagoservicios.service_payment.domain.exception.ModelNotFoundException;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Service;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.ServiceStatus;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.ServiceRepositoryPort;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetServicesInteractorTest {

    @Mock
    private ServiceRepositoryPort serviceRepositoryPort;

    private GetServicesInteractor interactor;
    private List<Service> serviceListMock;
    private Service serviceMock;

    @BeforeEach
    void setUp() {
        interactor = new GetServicesInteractor(serviceRepositoryPort);

        serviceMock = Service.builder()
                .id("SERVICE-001")
                .name("Service Name")
                .type("SERVICE_TYPE")
                .label("Service Label")
                .length(10)
                .dataType("STRING")
                .status(ServiceStatus.CREATED)
                .build();

        serviceListMock = new ArrayList<>();
        serviceListMock.add(serviceMock);
    }

    @Test
    void execute_whenServicesExist_shouldReturnServiceList() {
        when(serviceRepositoryPort.findAllByLastSyncAndRecipient("RECIPIENT-001"))
                .thenReturn(serviceListMock);

        List<Service> result = interactor.execute("RECIPIENT-001");

        verify(serviceRepositoryPort).findAllByLastSyncAndRecipient("RECIPIENT-001");
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("SERVICE-001", result.get(0).getId());
    }

    @Test
    void execute_whenServicesExist_shouldCallServiceRepositoryPortWithCorrectRecipientId() {
        when(serviceRepositoryPort.findAllByLastSyncAndRecipient("RECIPIENT-001"))
                .thenReturn(serviceListMock);

        interactor.execute("RECIPIENT-001");

        verify(serviceRepositoryPort).findAllByLastSyncAndRecipient("RECIPIENT-001");
    }

    @Test
    void execute_whenServicesListIsEmpty_shouldThrowModelNotFoundException() {
        when(serviceRepositoryPort.findAllByLastSyncAndRecipient("RECIPIENT-001"))
                .thenReturn(new ArrayList<>());

        ModelNotFoundException exception = assertThrows(ModelNotFoundException.class, () ->
                interactor.execute("RECIPIENT-001")
        );

        assertEquals("Services not found for recipientId: RECIPIENT-001", exception.getMessage());
    }

    @Test
    void execute_whenServicesListIsNull_shouldThrowModelNotFoundException() {
        when(serviceRepositoryPort.findAllByLastSyncAndRecipient("RECIPIENT-001"))
                .thenReturn(null);

        ModelNotFoundException exception = assertThrows(ModelNotFoundException.class, () ->
                interactor.execute("RECIPIENT-001")
        );

        assertEquals("Services not found for recipientId: RECIPIENT-001", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"RECIPIENT-001", "RECIPIENT-002", "RECIPIENT-003"})
    void execute_whenRecipientIdProvided_shouldFetchServicesByRecipientId(String recipientId) {
        when(serviceRepositoryPort.findAllByLastSyncAndRecipient(recipientId))
                .thenReturn(serviceListMock);

        List<Service> result = interactor.execute(recipientId);

        verify(serviceRepositoryPort).findAllByLastSyncAndRecipient(recipientId);
        assertNotNull(result);
    }
}

