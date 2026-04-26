package org.example.skaria.elasticsearch.springboot3elasticsearch.dto;

public record Pagination(int page,
                         int size,
                         long totalElements,
                         int totalPages) {
}
