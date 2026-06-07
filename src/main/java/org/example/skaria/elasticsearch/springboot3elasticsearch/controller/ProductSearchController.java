package org.example.skaria.elasticsearch.springboot3elasticsearch.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.skaria.elasticsearch.springboot3elasticsearch.dto.ProductDTO;
import org.example.skaria.elasticsearch.springboot3elasticsearch.dto.SearchProductsRequestDTO;
import org.example.skaria.elasticsearch.springboot3elasticsearch.dto.SearchResponseDTO;
import org.example.skaria.elasticsearch.springboot3elasticsearch.entity.ProductDocument;
import org.example.skaria.elasticsearch.springboot3elasticsearch.mapper.ProductMapper;
import org.example.skaria.elasticsearch.springboot3elasticsearch.service.ProductManagementService;
import org.example.skaria.elasticsearch.springboot3elasticsearch.service.ProductSearchService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping(path = "/api/v1")
@RequiredArgsConstructor
public class ProductSearchController {

    private final ProductSearchService productSearchService;

    @GetMapping(path = "/products/search")
    public SearchResponseDTO search(SearchProductsRequestDTO searchProductsRequestDTO) {
        return productSearchService.searchProducts(searchProductsRequestDTO);
    }
}
