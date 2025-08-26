package ru.dmitartur.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.dmitartur.entity.Warehouse;
import ru.dmitartur.repository.WarehouseRepository;
import ru.dmitartur.common.utils.JwtUtil;
import ru.dmitartur.dto.WarehouseDto;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WarehouseService {
    
    private final WarehouseRepository warehouseRepository;
    
    /**
     * Получить все склады компании пользователя
     */
    public List<Warehouse> getCompanyWarehouses(UUID companyId) {
        UUID userId = JwtUtil.getRequiredOwnerId();
        return warehouseRepository.findByCompanyIdAndUserId(companyId, userId);
    }
    
    /**
     * Получить домашний склад компании пользователя
     */
    public Warehouse getHomeWarehouse(UUID companyId) {
        UUID userId = JwtUtil.getRequiredOwnerId();
        return warehouseRepository.findByCompanyIdAndUserIdAndIsHomeWarehouseTrueAndIsActiveTrue(companyId, userId)
                .orElseThrow(() -> new RuntimeException("Домашний склад не найден для компании: " + companyId));
    }
    
    /**
     * Получить FBS склады компании пользователя
     */
    public List<Warehouse> getFbsWarehouses(UUID companyId) {
        UUID userId = JwtUtil.getRequiredOwnerId();
        return warehouseRepository.findFbsWarehousesByCompanyIdAndUserId(companyId, userId);
    }
    
    /**
     * Получить FBO склады компании пользователя
     */
    public List<Warehouse> getFboWarehouses(UUID companyId) {
        UUID userId = JwtUtil.getRequiredOwnerId();
        return warehouseRepository.findFboWarehousesByCompanyIdAndUserId(companyId, userId);
    }
    
    /**
     * Проверить, что все склады принадлежат текущему пользователю
     */
    public void validateWarehousesOwnership(Set<WarehouseDto> warehouseDtos) {
        if (warehouseDtos == null || warehouseDtos.isEmpty()) {
            return;
        }
        
        UUID userId = JwtUtil.getRequiredOwnerId();
        
        for (WarehouseDto warehouseDto : warehouseDtos) {
            Warehouse warehouse = warehouseRepository.findById(warehouseDto.getId())
                    .orElseThrow(() -> new RuntimeException("Склад не найден: " + warehouseDto.getId()));
            
            if (!warehouse.getUserId().equals(userId)) {
                throw new RuntimeException("Склад не принадлежит пользователю: " + warehouseDto.getId());
            }
        }
    }
    
    /**
     * Создать новый склад
     */
    public Warehouse createWarehouse(Warehouse warehouse) {
        UUID userId = JwtUtil.getRequiredOwnerId();
        warehouse.setUserId(userId);
        
        // Проверяем, что для домашнего склада указан externalWarehouseId
        if (warehouse.isHomeWarehouse() && (warehouse.getExternalWarehouseId() == null || warehouse.getExternalWarehouseId().trim().isEmpty())) {
            throw new RuntimeException("Для домашнего склада обязательно указать externalWarehouseId");
        }
        
        // Проверяем, что у компании еще нет домашнего склада, если создаем новый
        if (warehouse.isHomeWarehouse() && warehouseRepository.existsByCompanyIdAndUserIdAndIsHomeWarehouseTrueAndIsActiveTrue(warehouse.getCompanyId(), userId)) {
            throw new RuntimeException("У компании уже есть домашний склад");
        }
        
        return warehouseRepository.save(warehouse);
    }
    
    /**
     * Обновить склад
     */
    public Warehouse updateWarehouse(Warehouse warehouse) {
        Warehouse existing = warehouseRepository.findById(warehouse.getId())
                .orElseThrow(() -> new RuntimeException("Склад не найден: " + warehouse.getId()));
        
        // Проверяем, что для домашнего склада указан externalWarehouseId
        if (warehouse.isHomeWarehouse() && (warehouse.getExternalWarehouseId() == null || warehouse.getExternalWarehouseId().trim().isEmpty())) {
            throw new RuntimeException("Для домашнего склада обязательно указать externalWarehouseId");
        }
        
        // Обновляем поля
        existing.setName(warehouse.getName());
        existing.setDescription(warehouse.getDescription());
        existing.setWarehouseType(warehouse.getWarehouseType());
        existing.setExternalWarehouseId(warehouse.getExternalWarehouseId());
        existing.setHomeWarehouse(warehouse.isHomeWarehouse());
        existing.setNotes(warehouse.getNotes());
        existing.setActive(warehouse.isActive());
        existing.setMarketplace(warehouse.getMarketplace());
        
        return warehouseRepository.save(existing);
    }
    
    /**
     * Удалить склад (деактивировать)
     */
    public void deleteWarehouse(UUID id) {
        UUID userId = JwtUtil.getRequiredOwnerId();
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Склад не найден: " + id));
        
        // Проверяем, что склад принадлежит пользователю
        if (!warehouse.getUserId().equals(userId)) {
            throw new RuntimeException("Склад не принадлежит пользователю");
        }
        
        warehouse.setActive(false);
        warehouseRepository.save(warehouse);
    }
    
    /**
     * Получить склад по ID
     */
    public Warehouse getWarehouse(UUID id) {
        UUID userId = JwtUtil.getRequiredOwnerId();
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Склад не найден: " + id));
        
        // Проверяем, что склад принадлежит пользователю
        if (!warehouse.getUserId().equals(userId)) {
            throw new RuntimeException("Склад не принадлежит пользователю");
        }
        
        return warehouse;
    }
}
