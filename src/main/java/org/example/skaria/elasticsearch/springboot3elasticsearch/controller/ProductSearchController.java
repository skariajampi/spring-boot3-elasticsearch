package org.example.skaria.elasticsearch.springboot3elasticsearch.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.skaria.elasticsearch.springboot3elasticsearch.dto.SearchProductsRequestDTO;
import org.example.skaria.elasticsearch.springboot3elasticsearch.dto.SearchResponseDTO;
import org.example.skaria.elasticsearch.springboot3elasticsearch.service.ProductSearchService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping(path = "/api/v1")
@RequiredArgsConstructor
public class ProductSearchController {

    private final ProductSearchService productSearchService;

    @GetMapping(path = "/products/search")
    public SearchResponseDTO search(@ParameterObject SearchProductsRequestDTO searchProductsRequestDTO) {
        return productSearchService.searchProducts(searchProductsRequestDTO);
    }
}
