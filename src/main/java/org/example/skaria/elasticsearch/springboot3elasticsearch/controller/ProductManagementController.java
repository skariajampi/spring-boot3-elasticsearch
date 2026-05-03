package org.example.skaria.elasticsearch.springboot3elasticsearch.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.skaria.elasticsearch.springboot3elasticsearch.model.ProductDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping(path = "/api/v1")
public class ProductManagementController {

    @GetMapping(path = "/products/{id}")
    public String getProductById(String id) {
        return "Product with ID " + id + " retrieved successfully.";
    }

    @PostMapping(path = "/products")
    public String createProduct(@Valid @RequestBody ProductDTO product) {
        return "Product with ID " +  " retrieved successfully.";
    }
}
