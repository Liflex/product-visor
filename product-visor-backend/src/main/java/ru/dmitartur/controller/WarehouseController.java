package ru.dmitartur.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.dmitartur.entity.Warehouse;
import ru.dmitartur.dto.WarehouseDto;
import ru.dmitartur.mapper.WarehouseMapper;
import ru.dmitartur.service.WarehouseService;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/warehouses")
@RequiredArgsConstructor
public class WarehouseController {
    
    private final WarehouseService warehouseService;
    private final WarehouseMapper warehouseMapper;
    
    /**
     * Получить все склады компании пользователя
     */
    @GetMapping("/company/{companyId}")
    public ResponseEntity<List<WarehouseDto>> getCompanyWarehouses(@PathVariable UUID companyId) {
        log.info("📦 Getting warehouses for company: {}", companyId);
        List<Warehouse> warehouses = warehouseService.getCompanyWarehouses(companyId);
        return ResponseEntity.ok(warehouses.stream().map(warehouseMapper::toDto).toList());
    }
    
    /**
     * Получить домашний склад компании пользователя
     */
    @GetMapping("/company/{companyId}/home")
    public ResponseEntity<WarehouseDto> getHomeWarehouse(@PathVariable UUID companyId) {
        log.info("🏠 Getting home warehouse for company: {}", companyId);
        Warehouse warehouse = warehouseService.getHomeWarehouse(companyId);
        return ResponseEntity.ok(warehouseMapper.toDto(warehouse));
    }
    
    /**
     * Получить FBS склады компании пользователя
     */
    @GetMapping("/company/{companyId}/fbs")
    public ResponseEntity<List<WarehouseDto>> getFbsWarehouses(@PathVariable UUID companyId) {
        log.info("📦 Getting FBS warehouses for company: {}", companyId);
        List<Warehouse> warehouses = warehouseService.getFbsWarehouses(companyId);
        return ResponseEntity.ok(warehouses.stream().map(warehouseMapper::toDto).toList());
    }
    
    /**
     * Получить FBO склады компании пользователя
     */
    @GetMapping("/company/{companyId}/fbo")
    public ResponseEntity<List<WarehouseDto>> getFboWarehouses(@PathVariable UUID companyId) {
        log.info("📦 Getting FBO warehouses for company: {}", companyId);
        List<Warehouse> warehouses = warehouseService.getFboWarehouses(companyId);
        return ResponseEntity.ok(warehouses.stream().map(warehouseMapper::toDto).toList());
    }
    
    /**
     * Создать новый склад
     */
    @PostMapping
    public ResponseEntity<WarehouseDto> createWarehouse(@RequestBody WarehouseDto warehouseDto) {
        log.info("➕ Creating warehouse: {}", warehouseDto.getName());
        Warehouse created = warehouseService.createWarehouse(warehouseMapper.toEntity(warehouseDto));
        return ResponseEntity.ok(warehouseMapper.toDto(created));
    }
    
    /**
     * Обновить склад
     */
    @PutMapping("/{id}")
    public ResponseEntity<WarehouseDto> updateWarehouse(@PathVariable UUID id, @RequestBody WarehouseDto warehouseDto) {
        log.info("✏️ Updating warehouse: {}", id);
        Warehouse warehouse = warehouseMapper.toEntity(warehouseDto);
        warehouse.setId(id);
        Warehouse updated = warehouseService.updateWarehouse(warehouse);
        return ResponseEntity.ok(warehouseMapper.toDto(updated));
    }
    
    /**
     * Удалить склад (деактивировать)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWarehouse(@PathVariable UUID id) {
        log.info("🗑️ Deleting warehouse: {}", id);
        warehouseService.deleteWarehouse(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * Получить склад по ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<WarehouseDto> getWarehouse(@PathVariable UUID id) {
        log.info("📦 Getting warehouse: {}", id);
        Warehouse warehouse = warehouseService.getWarehouse(id);
        return ResponseEntity.ok(warehouseMapper.toDto(warehouse));
    }
}

