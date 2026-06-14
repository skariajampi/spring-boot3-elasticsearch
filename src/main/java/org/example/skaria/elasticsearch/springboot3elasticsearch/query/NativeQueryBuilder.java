package org.example.skaria.elasticsearch.springboot3elasticsearch.query;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.extern.slf4j.Slf4j;
import org.example.skaria.elasticsearch.springboot3elasticsearch.dto.SearchProductsRequestDTO;
import org.example.skaria.elasticsearch.springboot3elasticsearch.entity.ProductDocument;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.example.skaria.elasticsearch.springboot3elasticsearch.query.ElasticSearchUtil.buildTermsAggregation;

@Slf4j
public class NativeQueryBuilder {

    private NativeQueryBuilder(){}

    private static final List<QueryRule> FILTER_QUERY_RULES = List.of(
            QueryRules.RATING_QUERY,
            QueryRules.BRAND_QUERY
    );

    private static final List<QueryRule> MUST_QUERY_RULES = List.of(
            QueryRules.SEARCH_QUERY
    );

    private static final List<QueryRule> SHOULD_QUERY_RULES = List.of(
            QueryRules.CATEGORY_QUERY
    );

    public static NativeQuery buildSearchQuery(SearchProductsRequestDTO searchProductsRequestDTO){

        //build filter queries
        List<Query> filterQueries = buildQueries(FILTER_QUERY_RULES, searchProductsRequestDTO);
        log.info("Filter Queries: {}", filterQueries);

        //build must queries
        List<Query> mustQueries = buildQueries(MUST_QUERY_RULES, searchProductsRequestDTO);
        log.info("Must Queries: {}", mustQueries);
        //build should queries
        List<Query> shouldQueries = buildQueries(SHOULD_QUERY_RULES, searchProductsRequestDTO);
        log.info("Should Queries: {}", shouldQueries);
        //build bool query
        BoolQuery boolQuery = BoolQuery.of(builder -> builder.filter(filterQueries)
                .must(mustQueries)
                .should(shouldQueries));
        return NativeQuery.builder()
                .withQuery(Query.of(builder -> builder.bool(boolQuery)))
                .withAggregation(ProductDocument.Fields.Aggregation.BRAND.getValue(),
                        buildTermsAggregation(ProductDocument.Fields.Brand.KEYWORD.getValue()))
                .withPageable(PageRequest.of(searchProductsRequestDTO.getPage(),searchProductsRequestDTO.getSize()))
                .withTrackTotalHits(true)
                .build();
    }

    private static List<Query> buildQueries(List<QueryRule> queryRuleList, SearchProductsRequestDTO searchProductsRequestDTO){
        return queryRuleList.stream()
                .map(queryRule -> queryRule.build(searchProductsRequestDTO))
                .flatMap(Optional::stream)
                .toList();
    }
}
