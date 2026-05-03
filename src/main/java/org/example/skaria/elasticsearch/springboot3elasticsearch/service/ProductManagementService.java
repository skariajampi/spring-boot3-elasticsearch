package org.example.skaria.elasticsearch.springboot3elasticsearch.service;

import org.example.skaria.elasticsearch.springboot3elasticsearch.entity.ProductDocument;

import java.util.Optional;

public interface ProductManagementService {

    ProductDocument saveProduct(ProductDocument productDocument);

    Optional<ProductDocument> findById(String id);

    boolean deleteProduct(String id);

    boolean exists(String id);
}
