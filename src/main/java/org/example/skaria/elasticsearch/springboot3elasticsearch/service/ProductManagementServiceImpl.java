package org.example.skaria.elasticsearch.springboot3elasticsearch.service;

import lombok.RequiredArgsConstructor;
import org.example.skaria.elasticsearch.springboot3elasticsearch.entity.ProductDocument;
import org.example.skaria.elasticsearch.springboot3elasticsearch.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductManagementServiceImpl implements ProductManagementService{

    private final ProductRepository productRepository; // Your ElasticsearchRepository

    @Override
    public ProductDocument saveProduct(ProductDocument productDocument) {
       return productRepository.save(productDocument);
    }

    public Optional<ProductDocument> findById(String id) {
        return productRepository.findById(id);
    }

    @Override
    public boolean deleteProduct(String id) {
        productRepository.deleteById(id);
        return true;
    }

    @Override
    public boolean exists(String id) {
        return productRepository.existsById(id);
    }

}