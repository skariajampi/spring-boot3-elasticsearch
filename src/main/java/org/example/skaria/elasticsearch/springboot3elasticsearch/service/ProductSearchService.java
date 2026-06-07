package org.example.skaria.elasticsearch.springboot3elasticsearch.service;

import org.example.skaria.elasticsearch.springboot3elasticsearch.dto.SearchProductsRequestDTO;
import org.example.skaria.elasticsearch.springboot3elasticsearch.dto.SearchResponseDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductSearchService {

    SearchResponseDTO searchProducts(SearchProductsRequestDTO searchProductsRequestDTO);

    // Suggestion logic
    List<String> getAutocompleteSuggestions(String partialQuery);
}
