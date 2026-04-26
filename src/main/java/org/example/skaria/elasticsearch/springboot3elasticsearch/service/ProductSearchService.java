package org.example.skaria.elasticsearch.springboot3elasticsearch.service;

import org.example.skaria.elasticsearch.springboot3elasticsearch.dto.SearchResponse;
import org.example.skaria.elasticsearch.springboot3elasticsearch.entity.ProductDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductSearchService {

    // Advanced Search
    SearchResponse searchProducts(String query, Pageable pageable);

    // Suggestion logic
    List<String> getAutocompleteSuggestions(String partialQuery);
}
