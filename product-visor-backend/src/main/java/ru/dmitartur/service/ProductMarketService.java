package ru.dmitartur.service;

import lombok.RequiredArgsConstructor;
import ru.dmitartur.dto.ProductMarketDto;
import ru.dmitartur.entity.ProductMarket;
import ru.dmitartur.mapper.ProductMarketMapper;
import ru.dmitartur.repository.ProductMarketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductMarketService {
    private final ProductMarketRepository productMarketRepository;
    private final ProductMarketMapper productMarketMapper;

    public List<ProductMarket> findAll() {
        return productMarketRepository.findAll();
    }

    public ProductMarket save(ProductMarket entity) {
        return productMarketRepository.save(entity);
    }

    public void deleteById(Long id) {
        productMarketRepository.deleteById(id);
    }

    public ProductMarket findById(Long id) {
        return productMarketRepository.findById(id).orElse(null);
    }

    public List<ProductMarket> findByProductId(Long productId) {
        return productMarketRepository.findByProductId(productId);
    }

    public List<ProductMarket> findByMarketId(Long marketId) {
        return productMarketRepository.findByMarketId(marketId);
    }

    // DTO методы
    public List<ProductMarketDto> findAllDto() {
        return productMarketRepository.findAll().stream()
                .map(productMarketMapper::toDto)
                .toList();
    }

    public ProductMarketDto saveDto(ProductMarketDto dto) {
        ProductMarket entity = productMarketMapper.toEntity(dto);
        ProductMarket saved = productMarketRepository.save(entity);
        return productMarketMapper.toDto(saved);
    }

    public List<ProductMarketDto> findByProductIdDto(Long productId) {
        return productMarketRepository.findByProductId(productId).stream()
                .map(productMarketMapper::toDto)
                .toList();
    }
} 