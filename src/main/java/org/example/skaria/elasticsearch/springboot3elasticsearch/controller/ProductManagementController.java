package org.example.skaria.elasticsearch.springboot3elasticsearch.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.skaria.elasticsearch.springboot3elasticsearch.entity.ProductDocument;
import org.example.skaria.elasticsearch.springboot3elasticsearch.mapper.ProductMapper;
import org.example.skaria.elasticsearch.springboot3elasticsearch.model.ProductDTO;
import org.example.skaria.elasticsearch.springboot3elasticsearch.service.ProductManagementService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping(path = "/api/v1")
@RequiredArgsConstructor
public class ProductManagementController {

    private final ProductManagementService productManagementService;
    private final ProductMapper productMapper;

    @GetMapping(path = "/products/{id}")
    public ResponseEntity<ProductDTO> getProductById(String id) {
        return productManagementService.findById(id)
                .map(productMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(path = "/products")
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductDTO product) {
        ProductDocument document = productMapper.toDocument(product);
        ProductDocument savedeProductDocument = productManagementService.saveProduct(document);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(productMapper.toDto(savedeProductDocument));
    }
}
