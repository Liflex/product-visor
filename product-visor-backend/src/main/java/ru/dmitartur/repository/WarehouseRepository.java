package ru.dmitartur.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.dmitartur.entity.Warehouse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, UUID> {
    
    /**
     * Найти все склады компании пользователя
     */
    List<Warehouse> findByCompanyIdAndUserIdAndIsActiveTrue(UUID companyId, UUID userId);

    /**
     * Найти все склады компании пользователя
     */
    List<Warehouse> findByCompanyIdAndUserId(UUID companyId, UUID userId);
    
    /**
     * Найти домашний склад компании пользователя
     */
    Optional<Warehouse> findByCompanyIdAndUserIdAndIsHomeWarehouseTrueAndIsActiveTrue(UUID companyId, UUID userId);
    
    /**
     * Найти все FBS склады компании пользователя
     */
    @Query("SELECT w FROM Warehouse w WHERE w.companyId = :companyId AND w.userId = :userId AND w.warehouseType = 'FBS' AND w.isActive = true")
    List<Warehouse> findFbsWarehousesByCompanyIdAndUserId(@Param("companyId") UUID companyId, @Param("userId") UUID userId);
    
    /**
     * Найти все FBO склады компании пользователя
     */
    @Query("SELECT w FROM Warehouse w WHERE w.companyId = :companyId AND w.userId = :userId AND w.warehouseType = 'FBO' AND w.isActive = true")
    List<Warehouse> findFboWarehousesByCompanyIdAndUserId(@Param("companyId") UUID companyId, @Param("userId") UUID userId);
    
    /**
     * Найти склад по внешнему ID
     */
    Optional<Warehouse> findByExternalWarehouseIdAndIsActiveTrue(String externalWarehouseId);
    
    /**
     * Проверить, существует ли домашний склад для компании пользователя
     */
    boolean existsByCompanyIdAndUserIdAndIsHomeWarehouseTrueAndIsActiveTrue(UUID companyId, UUID userId);
}
