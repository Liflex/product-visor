package org.example.mapper;

import javax.annotation.processing.Generated;
import org.example.dto.OrderDto;
import org.example.entity.Order;
import org.example.entity.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-08-08T22:15:31+0300",
    comments = "version: 1.6.2, compiler: Eclipse JDT (IDE) 3.42.50.v20250729-0351, environment: Java 21.0.7 (Azul Systems, Inc.)"
)
@Component
public class OrderMapperImpl implements OrderMapper {

    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private MarketMapper marketMapper;

    @Override
    public OrderDto toDto(Order order) {
        if ( order == null ) {
            return null;
        }

        OrderDto orderDto = new OrderDto();

        orderDto.setProduct( productMapper.toDto( order.getProduct() ) );
        orderDto.setMarket( marketMapper.toDto( order.getMarket() ) );
        orderDto.setId( order.getId() );
        orderDto.setOrderBarcode( order.getOrderBarcode() );
        orderDto.setOrderDate( order.getOrderDate() );
        orderDto.setPrice( order.getPrice() );
        orderDto.setStatus( order.getStatus() );

        return orderDto;
    }

    @Override
    public Order toEntity(OrderDto orderDto) {
        if ( orderDto == null ) {
            return null;
        }

        Order order = new Order();

        order.setProduct( productMapper.toEntity( orderDto.getProduct() ) );
        order.setMarket( marketMapper.toEntity( orderDto.getMarket() ) );
        order.setId( orderDto.getId() );
        order.setOrderBarcode( orderDto.getOrderBarcode() );
        order.setPrice( orderDto.getPrice() );

        order.setOrderDate( java.time.LocalDateTime.now() );
        order.setStatus( OrderStatus.PURCHASED );

        return order;
    }
}
