package org.example.skaria.elasticsearch.springboot3elasticsearch.service;

import org.example.skaria.elasticsearch.springboot3elasticsearch.model.SearchResponseDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductSearchService {

    // Advanced Search
    SearchResponseDTO searchProducts(String query, Pageable pageable);

    // Suggestion logic
    List<String> getAutocompleteSuggestions(String partialQuery);
}
