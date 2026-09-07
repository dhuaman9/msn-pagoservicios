package pe.financiera.bs.pagoservicios.service_payment.infrastructure.persistence.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Service;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.ServiceStatus;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.persistence.entity.ServicePaymentJpaEntity;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.persistence.repository.V2ServiceRepository;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicePersistenceAdapterTest {

    private ServicePersistenceAdapter servicePersistenceAdapter;

    private V2ServiceRepository serviceRepository;

    private String recipientId;
    private String serviceId;

    @BeforeEach
    void setUp() {
        serviceRepository = mock(V2ServiceRepository.class);
        servicePersistenceAdapter = new ServicePersistenceAdapter(serviceRepository);

        recipientId = "REC-001";
        serviceId = "SVC-001";
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findAllByLastSyncAndRecipient: Cuando findLastSync retorna null
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void findAllByLastSyncAndRecipient_whenLastSyncIsNull_shouldReturnEmptyList() {
        // Arrange
        when(serviceRepository.findLastSync(recipientId)).thenReturn(null);

        // Act
        List<Service> result = servicePersistenceAdapter.findAllByLastSyncAndRecipient(recipientId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findAllByLastSyncAndRecipient_whenLastSyncIsNull_shouldNotQueryServices() {
        // Arrange
        when(serviceRepository.findLastSync(recipientId)).thenReturn(null);

        // Act
        servicePersistenceAdapter.findAllByLastSyncAndRecipient(recipientId);

        // Assert
        verify(serviceRepository, never()).findAllBySynchronizationIdAndStatusAndRecipient_Id(
                anyLong(), any(ServiceStatus.class), anyString());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findAllByLastSyncAndRecipient: Cuando findLastSync retorna un valor válido
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void findAllByLastSyncAndRecipient_whenLastSyncValid_shouldCallRepositoryWithCorrectParameters() {
        // Arrange
        Long lastSync = 100L;
        when(serviceRepository.findLastSync(recipientId)).thenReturn(lastSync);
        when(serviceRepository.findAllBySynchronizationIdAndStatusAndRecipient_Id(
                lastSync, ServiceStatus.VALID, recipientId))
                .thenReturn(Collections.emptyList());

        // Act
        servicePersistenceAdapter.findAllByLastSyncAndRecipient(recipientId);

        // Assert
        verify(serviceRepository).findAllBySynchronizationIdAndStatusAndRecipient_Id(
                lastSync, ServiceStatus.VALID, recipientId);
    }

    @Test
    void findAllByLastSyncAndRecipient_whenLastSyncValid_shouldReturnMappedServices() {
        // Arrange
        Long lastSync = 100L;
        ServicePaymentJpaEntity serviceEntity = createServicePaymentJpaEntity();

        when(serviceRepository.findLastSync(recipientId)).thenReturn(lastSync);
        when(serviceRepository.findAllBySynchronizationIdAndStatusAndRecipient_Id(
                lastSync, ServiceStatus.VALID, recipientId))
                .thenReturn(List.of(serviceEntity));

        // Act
        List<Service> result = servicePersistenceAdapter.findAllByLastSyncAndRecipient(recipientId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("SVC-001", result.get(0).getId());
        assertEquals("Recarga Claro", result.get(0).getName());
    }

    @Test
    void findAllByLastSyncAndRecipient_whenLastSyncValid_shouldMapAllFields() {
        // Arrange
        Long lastSync = 100L;
        ServicePaymentJpaEntity serviceEntity = createServicePaymentJpaEntity();

        when(serviceRepository.findLastSync(recipientId)).thenReturn(lastSync);
        when(serviceRepository.findAllBySynchronizationIdAndStatusAndRecipient_Id(
                lastSync, ServiceStatus.VALID, recipientId))
                .thenReturn(List.of(serviceEntity));

        // Act
        List<Service> result = servicePersistenceAdapter.findAllByLastSyncAndRecipient(recipientId);

        // Assert
        Service mappedService = result.get(0);
        assertEquals("SVC-001", mappedService.getId());
        assertEquals("Recarga Claro", mappedService.getName());
        assertEquals("RECHARGE", mappedService.getType());
        assertEquals("Número de teléfono", mappedService.getLabel());
        assertEquals(9, mappedService.getLength());
        assertEquals("NUMERIC", mappedService.getDataType());
        assertEquals(ServiceStatus.VALID, mappedService.getStatus());
    }

    @Test
    void findAllByLastSyncAndRecipient_whenMultipleServices_shouldReturnAllMapped() {
        // Arrange
        Long lastSync = 100L;
        ServicePaymentJpaEntity serviceEntity1 = createServicePaymentJpaEntity();

        ServicePaymentJpaEntity serviceEntity2 = new ServicePaymentJpaEntity();
        serviceEntity2.setId("SVC-002");
        serviceEntity2.setName("Pago de agua");
        serviceEntity2.setType("PAYMENT");
        serviceEntity2.setLabel("Número de cuenta");
        serviceEntity2.setLength(12);
        serviceEntity2.setDataType("NUMERIC");
        serviceEntity2.setStatus(ServiceStatus.VALID);

        when(serviceRepository.findLastSync(recipientId)).thenReturn(lastSync);
        when(serviceRepository.findAllBySynchronizationIdAndStatusAndRecipient_Id(
                lastSync, ServiceStatus.VALID, recipientId))
                .thenReturn(List.of(serviceEntity1, serviceEntity2));

        // Act
        List<Service> result = servicePersistenceAdapter.findAllByLastSyncAndRecipient(recipientId);

        // Assert
        assertEquals(2, result.size());
        assertEquals("SVC-001", result.get(0).getId());
        assertEquals("SVC-002", result.get(1).getId());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findByRecipientAndServiceId: Cuando encuentra la entidad
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void findByRecipientAndServiceId_whenServiceFound_shouldReturnMappedService() {
        // Arrange
        ServicePaymentJpaEntity serviceEntity = createServicePaymentJpaEntity();
        when(serviceRepository.findFirstByRecipient_IdAndIdAndStatusOrderBySynchronizationIdDesc(
                recipientId, serviceId, ServiceStatus.VALID))
                .thenReturn(serviceEntity);

        // Act
        Service result = servicePersistenceAdapter.findByRecipientAndServiceId(recipientId, serviceId);

        // Assert
        assertNotNull(result);
        assertEquals("SVC-001", result.getId());
        assertEquals("Recarga Claro", result.getName());
    }

    @Test
    void findByRecipientAndServiceId_whenServiceFound_shouldCallRepositoryWithCorrectParameters() {
        // Arrange
        ServicePaymentJpaEntity serviceEntity = createServicePaymentJpaEntity();
        when(serviceRepository.findFirstByRecipient_IdAndIdAndStatusOrderBySynchronizationIdDesc(
                recipientId, serviceId, ServiceStatus.VALID))
                .thenReturn(serviceEntity);

        // Act
        servicePersistenceAdapter.findByRecipientAndServiceId(recipientId, serviceId);

        // Assert
        verify(serviceRepository).findFirstByRecipient_IdAndIdAndStatusOrderBySynchronizationIdDesc(
                recipientId, serviceId, ServiceStatus.VALID);
    }

    @Test
    void findByRecipientAndServiceId_whenServiceFound_shouldMapAllFieldsCorrectly() {
        // Arrange
        ServicePaymentJpaEntity serviceEntity = createServicePaymentJpaEntity();
        when(serviceRepository.findFirstByRecipient_IdAndIdAndStatusOrderBySynchronizationIdDesc(
                recipientId, serviceId, ServiceStatus.VALID))
                .thenReturn(serviceEntity);

        // Act
        Service result = servicePersistenceAdapter.findByRecipientAndServiceId(recipientId, serviceId);

        // Assert
        assertEquals("SVC-001", result.getId());
        assertEquals("Recarga Claro", result.getName());
        assertEquals("RECHARGE", result.getType());
        assertEquals("Número de teléfono", result.getLabel());
        assertEquals(9, result.getLength());
        assertEquals("NUMERIC", result.getDataType());
        assertEquals(ServiceStatus.VALID, result.getStatus());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findByRecipientAndServiceId: Cuando no encuentra la entidad
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void findByRecipientAndServiceId_whenServiceNotFound_shouldReturnNull() {
        // Arrange
        when(serviceRepository.findFirstByRecipient_IdAndIdAndStatusOrderBySynchronizationIdDesc(
                recipientId, serviceId, ServiceStatus.VALID))
                .thenReturn(null);

        // Act
        Service result = servicePersistenceAdapter.findByRecipientAndServiceId(recipientId, serviceId);

        // Assert
        assertNull(result);
    }

    @Test
    void findByRecipientAndServiceId_whenServiceNotFound_shouldCallRepositoryCorrectly() {
        // Arrange
        when(serviceRepository.findFirstByRecipient_IdAndIdAndStatusOrderBySynchronizationIdDesc(
                recipientId, serviceId, ServiceStatus.VALID))
                .thenReturn(null);

        // Act
        servicePersistenceAdapter.findByRecipientAndServiceId(recipientId, serviceId);

        // Assert
        verify(serviceRepository).findFirstByRecipient_IdAndIdAndStatusOrderBySynchronizationIdDesc(
                recipientId, serviceId, ServiceStatus.VALID);
    }

    @Test
    void findByRecipientAndServiceId_whenRepositoryReturnsNull_shouldHandleGracefully() {
        // Arrange
        when(serviceRepository.findFirstByRecipient_IdAndIdAndStatusOrderBySynchronizationIdDesc(
                "REC-999", "SVC-999", ServiceStatus.VALID))
                .thenReturn(null);

        // Act
        Service result = servicePersistenceAdapter.findByRecipientAndServiceId("REC-999", "SVC-999");

        // Assert
        assertNull(result);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helper methods
    // ─────────────────────────────────────────────────────────────────────────

    private ServicePaymentJpaEntity createServicePaymentJpaEntity() {
        ServicePaymentJpaEntity entity = new ServicePaymentJpaEntity();
        entity.setId("SVC-001");
        entity.setName("Recarga Claro");
        entity.setType("RECHARGE");
        entity.setLabel("Número de teléfono");
        entity.setLength(9);
        entity.setDataType("NUMERIC");
        entity.setStatus(ServiceStatus.VALID);
        return entity;
    }
}

