package pe.financiera.bs.pagoservicios.service_payment.infrastructure.persistence.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.persistence.entity.SynchronizationJpaEntity;
import pe.financiera.bs.pagoservicios.service_payment.infrastructure.persistence.repository.V2SynchronizationRepository;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SynchronizationAdapterTest {

    private SynchronizationAdapter synchronizationAdapter;

    private V2SynchronizationRepository synchronizationRepository;

    private SynchronizationJpaEntity synchronizationEntityMock;

    @BeforeEach
    void setUp() {
        synchronizationRepository = mock(V2SynchronizationRepository.class);
        synchronizationAdapter = new SynchronizationAdapter(synchronizationRepository);

        synchronizationEntityMock = new SynchronizationJpaEntity();
        synchronizationEntityMock.setId(1L);
        synchronizationEntityMock.setCreatedTime(LocalDateTime.now());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Éxito con registro encontrado
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void getLastSynchronizationDate_whenRecordFound_shouldReturnSynchronizationEntity() {
        // Arrange
        when(synchronizationRepository.findFirstByOrderByCreatedTimeDesc())
                .thenReturn(synchronizationEntityMock);

        // Act
        SynchronizationJpaEntity result = synchronizationAdapter.getLastSynchronizationDate();

        // Assert
        assertNotNull(result);
        assertEquals(synchronizationEntityMock.getId(), result.getId());
    }

    @Test
    void getLastSynchronizationDate_whenRecordFound_shouldCallRepositoryMethod() {
        // Arrange
        when(synchronizationRepository.findFirstByOrderByCreatedTimeDesc())
                .thenReturn(synchronizationEntityMock);

        // Act
        synchronizationAdapter.getLastSynchronizationDate();

        // Assert
        verify(synchronizationRepository).findFirstByOrderByCreatedTimeDesc();
    }

    @Test
    void getLastSynchronizationDate_whenRecordFound_shouldReturnCorrectEntity() {
        // Arrange
        SynchronizationJpaEntity entity = new SynchronizationJpaEntity();
        entity.setId(5L);
        LocalDateTime createdTime = LocalDateTime.now();
        entity.setCreatedTime(createdTime);

        when(synchronizationRepository.findFirstByOrderByCreatedTimeDesc())
                .thenReturn(entity);

        // Act
        SynchronizationJpaEntity result = synchronizationAdapter.getLastSynchronizationDate();

        // Assert
        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertEquals(createdTime, result.getCreatedTime());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Sin registro encontrado (resultado nulo)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void getLastSynchronizationDate_whenNoRecordFound_shouldReturnNull() {
        // Arrange
        when(synchronizationRepository.findFirstByOrderByCreatedTimeDesc())
                .thenReturn(null);

        // Act
        SynchronizationJpaEntity result = synchronizationAdapter.getLastSynchronizationDate();

        // Assert
        assertNull(result);
    }

    @Test
    void getLastSynchronizationDate_whenNoRecordFound_shouldCallRepositoryMethod() {
        // Arrange
        when(synchronizationRepository.findFirstByOrderByCreatedTimeDesc())
                .thenReturn(null);

        // Act
        synchronizationAdapter.getLastSynchronizationDate();

        // Assert
        verify(synchronizationRepository).findFirstByOrderByCreatedTimeDesc();
    }

    @Test
    void getLastSynchronizationDate_whenNoRecordFound_shouldHandleNullGracefully() {
        // Arrange
        when(synchronizationRepository.findFirstByOrderByCreatedTimeDesc())
                .thenReturn(null);

        // Act & Assert - Should not throw exception
        SynchronizationJpaEntity result = synchronizationAdapter.getLastSynchronizationDate();

        assertNull(result);
    }
}

