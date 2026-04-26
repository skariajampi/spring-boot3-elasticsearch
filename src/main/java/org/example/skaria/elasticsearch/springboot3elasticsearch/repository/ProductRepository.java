package org.example.skaria.elasticsearch.springboot3elasticsearch.repository;

import org.example.skaria.elasticsearch.springboot3elasticsearch.entity.ProductDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends ElasticsearchRepository<ProductDocument, String> {

    Optional<ProductDocument> findBySku(String sku);
    List<ProductDocument> findByBrand(String brand);

}
