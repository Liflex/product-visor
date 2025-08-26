package ru.dmitartur.dto;

import lombok.Data;
import ru.dmitartur.common.enums.Marketplace;
import ru.dmitartur.entity.Warehouse.WarehouseType;

import java.util.UUID;

@Data
public class WarehouseDto {
    private UUID id;
    private UUID companyId;
    private String name;
    private String description;
    private WarehouseType warehouseType;
    private String externalWarehouseId;
    private Boolean isActive;
    private Boolean isHomeWarehouse;
    private String notes;
    private Marketplace marketplace;
}


