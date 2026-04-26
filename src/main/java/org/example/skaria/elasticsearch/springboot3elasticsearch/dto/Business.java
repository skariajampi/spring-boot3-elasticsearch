package org.example.skaria.elasticsearch.springboot3elasticsearch.dto;

import org.springframework.data.elasticsearch.annotations.Field;

import java.util.List;

public record Business(String id,
                       String name,
                       String description,
                       String address,
                       List<String> category,
                       List<String> offerings,
                       @Field(name = "avg_rating") Float rating,
                       @Field(name = "num_of_reviews") Integer reviewsCount,
                       String url) {
}
