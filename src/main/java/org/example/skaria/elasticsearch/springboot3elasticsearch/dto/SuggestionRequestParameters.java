package org.example.skaria.elasticsearch.springboot3elasticsearch.dto;

import org.example.skaria.elasticsearch.springboot3elasticsearch.exceptions.BadRequestException;
import org.springframework.util.StringUtils;

import java.util.Objects;

public record SuggestionRequestParameters(String prefix,
                                          Integer limit) {

    public SuggestionRequestParameters {
        if(!StringUtils.hasText(prefix)){
            throw new BadRequestException("prefix can not be empty");
        }
        limit = Objects.requireNonNullElse(limit, 10);
    }

}
