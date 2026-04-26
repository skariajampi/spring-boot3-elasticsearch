package org.example.skaria.elasticsearch.springboot3elasticsearch.service;

import lombok.RequiredArgsConstructor;
import org.example.skaria.elasticsearch.springboot3elasticsearch.model.SearchResponse;
import org.example.skaria.elasticsearch.springboot3elasticsearch.entity.ProductDocument;
import org.example.skaria.elasticsearch.springboot3elasticsearch.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHitSupport;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductSearchServiceImpl implements ProductSearchService{

    private final ProductRepository productRepository; // Your ElasticsearchRepository
    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public SearchResponse searchProducts(String query, Pageable pageable) {
        return null;
    }

    @Override
    public List<String> getAutocompleteSuggestions(String partialQuery) {
        return List.of();
    }


    public Optional<ProductDocument> findById(String id) {
        return productRepository.findById(id);
    }

}