package ru.dmitartur.ozon.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.dmitartur.ozon.integration.OzonSellerApi;

@Service
@RequiredArgsConstructor
public class OzonService {
    private final OzonSellerApi api;
    private final ObjectMapper mapper = new ObjectMapper();

    public JsonNode fboPostingList(JsonNode req) {
        return api.fboPostingList(req);
    }
}


