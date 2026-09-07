package pe.financiera.bs.pagoservicios.service_payment.application.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Alert;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Recipient;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.RecipientDashboard;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.RecipientStatus;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.AlertPort;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.RecipientRepositoryPort;
import pe.financiera.bs.pagoservicios.service_payment.domain.port.out.SynchronizationPort;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.persistence.entity.SynchronizationJpaEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FindLastRecipientsInteractorTest {

    private FindLastRecipientsInteractor interactor;

    private RecipientRepositoryPort recipientRepositoryPort;
    private SynchronizationPort synchronizationPort;
    private AlertPort alertPort;

    private SynchronizationJpaEntity syncEntityMock;
    private Alert alertMock;
    private List<Recipient> recipientsMock;

    @BeforeEach
    void setUp() {
        recipientRepositoryPort = mock(RecipientRepositoryPort.class);
        synchronizationPort = mock(SynchronizationPort.class);
        alertPort = mock(AlertPort.class);
        interactor = new FindLastRecipientsInteractor(recipientRepositoryPort, synchronizationPort, alertPort);

        syncEntityMock = SynchronizationJpaEntity.builder()
                .id(1L)
                .createdTime(LocalDateTime.of(2025, java.time.Month.JUNE, 15, 10, 30, 0))
                .build();

        alertMock = Alert.builder()
                .title("Alerta de prueba")
                .message("Mensaje de prueba")
                .type("INFO")
                .screen("HOME")
                .build();

        recipientsMock = List.of(
                Recipient.builder()
                        .generatedId(1L)
                        .id("REC-001")
                        .name("Beneficiario Uno")
                        .supports("BILL")
                        .status(RecipientStatus.VALID)
                        .top(true)
                        .build(),
                Recipient.builder()
                        .generatedId(2L)
                        .id("REC-002")
                        .name("Beneficiario Dos")
                        .supports("RECHARGE")
                        .status(RecipientStatus.VALID)
                        .top(false)
                        .build()
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Validación de userId (Assert.hasText)
    // ─────────────────────────────────────────────────────────────────────────

    @ParameterizedTest
    @MethodSource("provideInvalidUserIds")
    void execute_whenUserIdIsNullOrBlank_shouldThrowIllegalArgumentException(String invalidUserId) {
        assertThrows(IllegalArgumentException.class, () -> interactor.execute(invalidUserId));

        verify(synchronizationPort, never()).getLastSynchronizationDate();
        verify(recipientRepositoryPort, never()).findLatestRecipients(any());
        verify(alertPort, never()).getHomeAlert(anyString());
    }

    private static Stream<Arguments> provideInvalidUserIds() {
        String nullUserId = null;
        String emptyUserId = "";
        String blankUserId = "   ";

        return Stream.of(
                Arguments.of(nullUserId),
                Arguments.of(emptyUserId),
                Arguments.of(blankUserId)
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // synchronizationDate: lógica de nullability del syncEntity
    // ─────────────────────────────────────────────────────────────────────────

    @ParameterizedTest
    @MethodSource("provideNullSyncEntityScenarios")
    void execute_whenSyncEntityOrCreatedTimeIsNull_shouldReturnNullSynchronizationDate(
            SynchronizationJpaEntity syncEntity) {

        when(synchronizationPort.getLastSynchronizationDate()).thenReturn(syncEntity);
        when(recipientRepositoryPort.findLatestRecipients(any())).thenReturn(recipientsMock);
        when(alertPort.getHomeAlert(anyString())).thenReturn(alertMock);

        RecipientDashboard result = interactor.execute("user-001");

        assertNotNull(result);
        assertNull(result.getSynchronizationDate());
    }

    private static Stream<Arguments> provideNullSyncEntityScenarios() {
        SynchronizationJpaEntity nullSyncEntity = null;

        SynchronizationJpaEntity syncEntityWithNullTime = SynchronizationJpaEntity.builder()
                .id(1L)
                .createdTime(null)
                .build();

        return Stream.of(
                Arguments.of(nullSyncEntity),
                Arguments.of(syncEntityWithNullTime)
        );
    }

    @Test
    void execute_whenSyncEntityHasCreatedTime_shouldReturnFormattedSynchronizationDate() {
        when(synchronizationPort.getLastSynchronizationDate()).thenReturn(syncEntityMock);
        when(recipientRepositoryPort.findLatestRecipients(any())).thenReturn(recipientsMock);
        when(alertPort.getHomeAlert(anyString())).thenReturn(alertMock);

        RecipientDashboard result = interactor.execute("user-001");

        assertNotNull(result);
        assertEquals(syncEntityMock.getCreatedTime().toString(), result.getSynchronizationDate());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Flujo completo: happy path
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void execute_whenValidUserId_shouldReturnDashboardWithRecipientsAndAlert() {
        when(synchronizationPort.getLastSynchronizationDate()).thenReturn(syncEntityMock);
        when(recipientRepositoryPort.findLatestRecipients(syncEntityMock)).thenReturn(recipientsMock);
        when(alertPort.getHomeAlert("user-001")).thenReturn(alertMock);

        RecipientDashboard result = interactor.execute("user-001");

        assertNotNull(result);
        assertEquals(2, result.getRecipients().size());
        assertEquals(alertMock, result.getAlert());
        assertEquals(syncEntityMock.getCreatedTime().toString(), result.getSynchronizationDate());
    }

    @Test
    void execute_whenValidUserId_shouldCallAllPortsInOrder() {
        when(synchronizationPort.getLastSynchronizationDate()).thenReturn(syncEntityMock);
        when(recipientRepositoryPort.findLatestRecipients(any())).thenReturn(recipientsMock);
        when(alertPort.getHomeAlert(anyString())).thenReturn(alertMock);

        interactor.execute("user-001");

        verify(synchronizationPort).getLastSynchronizationDate();
        verify(recipientRepositoryPort).findLatestRecipients(syncEntityMock);
        verify(alertPort).getHomeAlert("user-001");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Escenarios parciales: alert y recipients nulos/vacíos
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void execute_whenAlertPortReturnsNull_shouldReturnDashboardWithNullAlert() {
        when(synchronizationPort.getLastSynchronizationDate()).thenReturn(syncEntityMock);
        when(recipientRepositoryPort.findLatestRecipients(any())).thenReturn(recipientsMock);
        when(alertPort.getHomeAlert(anyString())).thenReturn(null);

        RecipientDashboard result = interactor.execute("user-001");

        assertNotNull(result);
        assertNull(result.getAlert());
        assertEquals(2, result.getRecipients().size());
    }

    @Test
    void execute_whenRecipientsListIsEmpty_shouldReturnDashboardWithEmptyList() {
        when(synchronizationPort.getLastSynchronizationDate()).thenReturn(syncEntityMock);
        when(recipientRepositoryPort.findLatestRecipients(any())).thenReturn(List.of());
        when(alertPort.getHomeAlert(anyString())).thenReturn(alertMock);

        RecipientDashboard result = interactor.execute("user-001");

        assertNotNull(result);
        assertNotNull(result.getRecipients());
        assertEquals(0, result.getRecipients().size());
    }
}

