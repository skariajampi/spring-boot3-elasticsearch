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
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping(path = "/api/v1")
@RequiredArgsConstructor
public class ProductManagementController {

    private final ProductManagementService productManagementService;
    private final ProductMapper productMapper;

    @GetMapping(path = "/products/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable String id) {
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

    @DeleteMapping(path = "/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        productManagementService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping(path = "/products/{id}")
    public ResponseEntity<ProductDTO> updateProduct(@Valid @PathVariable String id,
                                                    @Valid @RequestBody ProductDTO product) {
        if(!productManagementService.exists(id)){
            return ResponseEntity.notFound().build();
        }
        ProductDocument document = productMapper.toDocument(product);
        document.setProductId(id);
        productManagementService.saveProduct(document);
        return ResponseEntity.ok(productMapper.toDto(document));
    }
}
