package pe.financiera.bs.pagoservicios.service_payment.infrastructure.persistence.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.Recipient;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.RecipientStatus;
import pe.financiera.bs.pagoservicios.service_payment.domain.model.RecipientType;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.persistence.entity.RecipientJpaEntity;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.persistence.entity.SynchronizationJpaEntity;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.persistence.mapper.RecipientPersistenceMapper;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.persistence.repository.V2RecipientRepository;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecipientPersistenceAdapterTest {

    private RecipientPersistenceAdapter recipientPersistenceAdapter;

    private V2RecipientRepository recipientRepository;
    private RecipientPersistenceMapper mapper;

    private SynchronizationJpaEntity synchronizationEntityMock;
    private RecipientJpaEntity recipientJpaEntityMock;
    private Recipient recipientDomainMock;

    @BeforeEach
    void setUp() {
        recipientRepository = mock(V2RecipientRepository.class);
        mapper = mock(RecipientPersistenceMapper.class);
        recipientPersistenceAdapter = new RecipientPersistenceAdapter(recipientRepository, mapper);

        synchronizationEntityMock = new SynchronizationJpaEntity();
        synchronizationEntityMock.setId(1L);

        recipientJpaEntityMock = new RecipientJpaEntity();
        recipientJpaEntityMock.setId("REC-001");
        recipientJpaEntityMock.setName("Claro");

        recipientDomainMock = Recipient.builder()
                .id("REC-001")
                .name("Claro")
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findLatestRecipients: Con sincronización válida
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void findLatestRecipients_whenSyncEntityWithValidId_shouldCallRepositoryWithSynchronizationId() {
        // Arrange
        when(recipientRepository.findLatestRecipients(
                RecipientType.PAYMENT.getType(),
                RecipientStatus.VALID.ordinal(),
                1
        )).thenReturn(List.of(recipientJpaEntityMock));
        when(mapper.toDomain(recipientJpaEntityMock)).thenReturn(recipientDomainMock);

        // Act
        List<Recipient> result = recipientPersistenceAdapter.findLatestRecipients(synchronizationEntityMock);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(recipientRepository).findLatestRecipients(
                RecipientType.PAYMENT.getType(),
                RecipientStatus.VALID.ordinal(),
                1
        );
    }

    @Test
    void findLatestRecipients_whenSyncEntityWithValidId_shouldMapResultsToDomain() {
        // Arrange
        when(recipientRepository.findLatestRecipients(
                RecipientType.PAYMENT.getType(),
                RecipientStatus.VALID.ordinal(),
                1
        )).thenReturn(List.of(recipientJpaEntityMock));
        when(mapper.toDomain(recipientJpaEntityMock)).thenReturn(recipientDomainMock);

        // Act
        List<Recipient> result = recipientPersistenceAdapter.findLatestRecipients(synchronizationEntityMock);

        // Assert
        assertEquals(recipientDomainMock.getId(), result.get(0).getId());
        assertEquals(recipientDomainMock.getName(), result.get(0).getName());
        verify(mapper).toDomain(recipientJpaEntityMock);
    }

    @Test
    void findLatestRecipients_whenSyncEntityWithValidId_shouldReturnMappedRecipientList() {
        // Arrange
        RecipientJpaEntity secondEntity = new RecipientJpaEntity();
        secondEntity.setId("REC-002");
        secondEntity.setName("Movistar");

        Recipient secondDomain = Recipient.builder()
                .id("REC-002")
                .name("Movistar")
                .build();

        when(recipientRepository.findLatestRecipients(
                RecipientType.PAYMENT.getType(),
                RecipientStatus.VALID.ordinal(),
                1
        )).thenReturn(List.of(recipientJpaEntityMock, secondEntity));
        when(mapper.toDomain(recipientJpaEntityMock)).thenReturn(recipientDomainMock);
        when(mapper.toDomain(secondEntity)).thenReturn(secondDomain);

        // Act
        List<Recipient> result = recipientPersistenceAdapter.findLatestRecipients(synchronizationEntityMock);

        // Assert
        assertEquals(2, result.size());
        assertEquals("REC-001", result.get(0).getId());
        assertEquals("REC-002", result.get(1).getId());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findLatestRecipients: Sin sincronización (syncEntity nulo o ID nulo)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void findLatestRecipients_whenSyncEntityIsNull_shouldCallRepositoryWithoutSynchronizationId() {
        // Arrange
        when(recipientRepository.findLatestRecipients(
                RecipientType.PAYMENT.getType(),
                RecipientStatus.VALID.ordinal()
        )).thenReturn(List.of(recipientJpaEntityMock));
        when(mapper.toDomain(recipientJpaEntityMock)).thenReturn(recipientDomainMock);

        // Act
        List<Recipient> result = recipientPersistenceAdapter.findLatestRecipients(null);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(recipientRepository).findLatestRecipients(
                RecipientType.PAYMENT.getType(),
                RecipientStatus.VALID.ordinal()
        );
    }

    @Test
    void findLatestRecipients_whenSyncEntityIdIsNull_shouldCallRepositoryWithoutSynchronizationId() {
        // Arrange
        SynchronizationJpaEntity syncEntityWithNullId = new SynchronizationJpaEntity();
        syncEntityWithNullId.setId(null);

        when(recipientRepository.findLatestRecipients(
                RecipientType.PAYMENT.getType(),
                RecipientStatus.VALID.ordinal()
        )).thenReturn(List.of(recipientJpaEntityMock));
        when(mapper.toDomain(recipientJpaEntityMock)).thenReturn(recipientDomainMock);

        // Act
        List<Recipient> result = recipientPersistenceAdapter.findLatestRecipients(syncEntityWithNullId);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(recipientRepository).findLatestRecipients(
                RecipientType.PAYMENT.getType(),
                RecipientStatus.VALID.ordinal()
        );
    }

    @Test
    void findLatestRecipients_whenNoSyncEntity_shouldReturnEmptyListWhenRepositoryReturnsEmpty() {
        // Arrange
        when(recipientRepository.findLatestRecipients(
                RecipientType.PAYMENT.getType(),
                RecipientStatus.VALID.ordinal()
        )).thenReturn(Collections.emptyList());

        // Act
        List<Recipient> result = recipientPersistenceAdapter.findLatestRecipients(null);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findLatestRecipientById: Cuando encuentra la entidad
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void findLatestRecipientById_whenRecipientFound_shouldReturnMappedRecipient() {
        // Arrange
        String recipientId = "REC-001";
        when(recipientRepository.findLatestRecipientById(recipientId, RecipientStatus.VALID.ordinal()))
                .thenReturn(recipientJpaEntityMock);
        when(mapper.toDomain(recipientJpaEntityMock)).thenReturn(recipientDomainMock);

        // Act
        Recipient result = recipientPersistenceAdapter.findLatestRecipientById(recipientId);

        // Assert
        assertNotNull(result);
        assertEquals(recipientDomainMock.getId(), result.getId());
        assertEquals(recipientDomainMock.getName(), result.getName());
    }

    @Test
    void findLatestRecipientById_whenRecipientFound_shouldCallRepositoryWithCorrectParameters() {
        // Arrange
        String recipientId = "REC-001";
        when(recipientRepository.findLatestRecipientById(recipientId, RecipientStatus.VALID.ordinal()))
                .thenReturn(recipientJpaEntityMock);
        when(mapper.toDomain(recipientJpaEntityMock)).thenReturn(recipientDomainMock);

        // Act
        recipientPersistenceAdapter.findLatestRecipientById(recipientId);

        // Assert
        verify(recipientRepository).findLatestRecipientById(recipientId, RecipientStatus.VALID.ordinal());
        verify(mapper).toDomain(recipientJpaEntityMock);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findLatestRecipientById: Cuando no encuentra la entidad
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void findLatestRecipientById_whenRecipientNotFound_shouldReturnNull() {
        // Arrange
        String recipientId = "REC-999";
        when(recipientRepository.findLatestRecipientById(recipientId, RecipientStatus.VALID.ordinal()))
                .thenReturn(null);

        // Act
        Recipient result = recipientPersistenceAdapter.findLatestRecipientById(recipientId);

        // Assert
        assertNull(result);
    }

    @Test
    void findLatestRecipientById_whenRepositoryReturnsNull_shouldNotCallMapper() {
        // Arrange
        String recipientId = "REC-999";
        when(recipientRepository.findLatestRecipientById(recipientId, RecipientStatus.VALID.ordinal()))
                .thenReturn(null);

        // Act
        recipientPersistenceAdapter.findLatestRecipientById(recipientId);

        // Assert
        verify(recipientRepository).findLatestRecipientById(recipientId, RecipientStatus.VALID.ordinal());
        // Mapper should not be called when entity is null
    }

    // ─────────────────────────────────────────────────────────────────────────
    // findLatestRechargeRecipients: Búsqueda de recipientes de recarga
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void findLatestRechargeRecipients_whenRechargeRecipientsExist_shouldReturnMappedList() {
        // Arrange
        RecipientJpaEntity rechargeEntity = new RecipientJpaEntity();
        rechargeEntity.setId("REC-RECHARGE-001");
        rechargeEntity.setName("Claro Recarga");

        Recipient rechargeDomain = Recipient.builder()
                .id("REC-RECHARGE-001")
                .name("Claro Recarga")
                .build();

        when(recipientRepository.findLatestRecipients(
                RecipientType.RECHARGE.getType(),
                RecipientStatus.VALID.ordinal()
        )).thenReturn(List.of(rechargeEntity));
        when(mapper.toDomain(rechargeEntity)).thenReturn(rechargeDomain);

        // Act
        List<Recipient> result = recipientPersistenceAdapter.findLatestRechargeRecipients();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("REC-RECHARGE-001", result.get(0).getId());
    }

    @Test
    void findLatestRechargeRecipients_whenRechargeRecipientsExist_shouldCallRepositoryWithRechargeType() {
        // Arrange
        when(recipientRepository.findLatestRecipients(
                RecipientType.RECHARGE.getType(),
                RecipientStatus.VALID.ordinal()
        )).thenReturn(List.of(recipientJpaEntityMock));
        when(mapper.toDomain(recipientJpaEntityMock)).thenReturn(recipientDomainMock);

        // Act
        recipientPersistenceAdapter.findLatestRechargeRecipients();

        // Assert
        verify(recipientRepository).findLatestRecipients(
                RecipientType.RECHARGE.getType(),
                RecipientStatus.VALID.ordinal()
        );
    }

    @Test
    void findLatestRechargeRecipients_whenNoRechargeRecipients_shouldReturnEmptyList() {
        // Arrange
        when(recipientRepository.findLatestRecipients(
                RecipientType.RECHARGE.getType(),
                RecipientStatus.VALID.ordinal()
        )).thenReturn(Collections.emptyList());

        // Act
        List<Recipient> result = recipientPersistenceAdapter.findLatestRechargeRecipients();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findLatestRechargeRecipients_whenMultipleRechargeRecipients_shouldReturnAllMapped() {
        // Arrange
        RecipientJpaEntity secondRechargeEntity = new RecipientJpaEntity();
        secondRechargeEntity.setId("REC-RECHARGE-002");
        secondRechargeEntity.setName("Movistar Recarga");

        Recipient secondRechargeDomain = Recipient.builder()
                .id("REC-RECHARGE-002")
                .name("Movistar Recarga")
                .build();

        when(recipientRepository.findLatestRecipients(
                RecipientType.RECHARGE.getType(),
                RecipientStatus.VALID.ordinal()
        )).thenReturn(List.of(recipientJpaEntityMock, secondRechargeEntity));
        when(mapper.toDomain(recipientJpaEntityMock)).thenReturn(recipientDomainMock);
        when(mapper.toDomain(secondRechargeEntity)).thenReturn(secondRechargeDomain);

        // Act
        List<Recipient> result = recipientPersistenceAdapter.findLatestRechargeRecipients();

        // Assert
        assertEquals(2, result.size());
        assertEquals("REC-001", result.get(0).getId());
        assertEquals("REC-RECHARGE-002", result.get(1).getId());
    }
}

