package org.example.skaria.elasticsearch.springboot3elasticsearch.dto;

import org.example.skaria.elasticsearch.springboot3elasticsearch.exceptions.BadRequestException;
import org.springframework.util.StringUtils;

import java.util.Objects;

/*
For an application with 1000s of attributes, we can use Map<K,V>.
Walmart follows this:
https://www.walmart.com/search?q=candy&facet=flavor:Chocolate||customer_rating:4 - 5 Stars||brand:Hershey's
facet field contains key1:value1||key2:value2||
* */

public record SearchRequestParameters(String query,
                                      String distance,
                                      Double latitude,
                                      Double longitude,
                                      Double rating,
                                      String state,
                                      String offerings,
                                      Integer page,  // 0 indexed
                                      Integer size){

    public SearchRequestParameters {
        if(!StringUtils.hasText(query)){
            throw new BadRequestException("query can not be empty");
        }
        page = Objects.requireNonNullElse(page, 0);
        size = Objects.requireNonNullElse(size, 10);
    }

}
