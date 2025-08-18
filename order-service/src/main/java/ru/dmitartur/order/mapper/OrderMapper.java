package ru.dmitartur.order.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.dmitartur.common.dto.OrderDto;
import ru.dmitartur.order.entity.Order;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "postingNumber", target = "postingNumber")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "createdAt", target = "createdAt")
    Order toEntity(String postingNumber, String status, LocalDateTime createdAt);

    @Mapping(source = "id", target = "id")
    @Mapping(source = "postingNumber", target = "postingNumber")
    @Mapping(source = "source", target = "source")
    @Mapping(source = "market", target = "market")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "createdAt", target = "createdAt")
    @Mapping(source = "updatedAt", target = "updatedAt")
    @Mapping(source = "ozonCreatedAt", target = "ozonCreatedAt")
    @Mapping(source = "customerName", target = "customerName")
    @Mapping(source = "customerPhone", target = "customerPhone")
    @Mapping(source = "address", target = "address")
    @Mapping(source = "totalPrice", target = "totalPrice")
    @Mapping(source = "items", target = "items")
    @Mapping(source = "inProcessAt", target = "inProcessAt")
    @Mapping(source = "shipmentDate", target = "shipmentDate")
    @Mapping(source = "deliveringDate", target = "deliveringDate")
    @Mapping(source = "cancelReason", target = "cancelReason")
    @Mapping(source = "cancelReasonId", target = "cancelReasonId")
    @Mapping(source = "cancellationType", target = "cancellationType")
    @Mapping(source = "trackingNumber", target = "trackingNumber")
    @Mapping(source = "deliveryMethodName", target = "deliveryMethodName")
    @Mapping(source = "substatus", target = "substatus")
    @Mapping(source = "isExpress", target = "isExpress")
    OrderDto toDto(Order order);

    @Named("now")
    default LocalDateTime now() { return LocalDateTime.now(); }
}






