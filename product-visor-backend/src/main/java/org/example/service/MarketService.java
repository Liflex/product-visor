package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.MarketDto;
import org.example.entity.Market;
import org.example.mapper.MarketMapper;
import org.example.repository.MarketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MarketService {
    private final MarketRepository marketRepository;
    private final MarketMapper marketMapper;

    public List<Market> findAll() {
        return marketRepository.findAll();
    }

    public Market save(Market entity) {
        return marketRepository.save(entity);
    }

    public void deleteById(Long id) {
        marketRepository.deleteById(id);
    }

    public Market findById(Long id) {
        return marketRepository.findById(id).orElse(null);
    }

    // DTO методы
    public List<MarketDto> findAllDto() {
        return marketRepository.findAll().stream()
                .map(marketMapper::toDto)
                .toList();
    }

    public MarketDto saveDto(MarketDto dto) {
        Market entity = marketMapper.toEntity(dto);
        Market saved = marketRepository.save(entity);
        return marketMapper.toDto(saved);
    }

    public MarketDto findByIdDto(Long id) {
        Market market = marketRepository.findById(id).orElse(null);
        return market != null ? marketMapper.toDto(market) : null;
    }
} 