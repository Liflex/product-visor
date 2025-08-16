package ru.dmitartur.ozon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.dmitartur.ozon.entity.SyncCheckpoint;

import java.util.Optional;

@Repository
public interface SyncCheckpointRepository extends JpaRepository<SyncCheckpoint, Long> {
    
    /**
     * Найти точку синхронизации по имени
     */
    Optional<SyncCheckpoint> findByCheckpointName(String checkpointName);
    
    /**
     * Проверить существование точки синхронизации по имени
     */
    boolean existsByCheckpointName(String checkpointName);
}
