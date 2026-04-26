package org.example.skaria.elasticsearch.springboot3elasticsearch.dto;

import java.util.List;

public record Facet(String name,
                    List<FacetItem> items) {
}

