package org.example.skaria.elasticsearch.springboot3elasticsearch.query;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.TermsAggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;

public class ElasticSearchUtil {

    private ElasticSearchUtil(){}

    public static Query buildTermQuery(String field, String value, float boost){
        TermQuery termQuery = TermQuery.of(builder -> builder
                .field(field)
                .value(value)
                .boost(boost)
                .caseInsensitive(true));

        return Query.of(builder -> builder.term(termQuery));
    }

    public static Aggregation buildTermsAggregation(String field){
        var termsAggregation = TermsAggregation.of(builder -> builder.field(field).size(10));
        return Aggregation.of(builder -> builder.terms(termsAggregation));
    }
}
