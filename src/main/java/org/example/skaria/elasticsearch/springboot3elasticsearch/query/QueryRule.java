package org.example.skaria.elasticsearch.springboot3elasticsearch.query;

import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import org.example.skaria.elasticsearch.springboot3elasticsearch.dto.SearchProductsRequestDTO;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public record QueryRule(Predicate<SearchProductsRequestDTO> predicate,
                        Function<SearchProductsRequestDTO, Query> function) {

    public static QueryRule of(Predicate<SearchProductsRequestDTO> predicate, Function<SearchProductsRequestDTO, Query> function){
        return new QueryRule(predicate, function);
    }

    public Optional<Query> build(SearchProductsRequestDTO searchProductsRequestDTO){
        return Optional.of(searchProductsRequestDTO)
                .filter(this.predicate())
                .map(this.function());
    }
}
