package org.example.skaria.elasticsearch.springboot3elasticsearch.dto;

import generated.model.ProductResponseDTO;

import java.util.List;

public record SearchResponse(List<ProductResponseDTO> results,
                             List<Facet> facets,
                             Pagination pagination,
                             long timeTaken) {
}