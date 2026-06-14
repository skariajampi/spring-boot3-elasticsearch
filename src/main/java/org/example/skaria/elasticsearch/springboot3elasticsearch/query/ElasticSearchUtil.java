package org.example.skaria.elasticsearch.springboot3elasticsearch.query;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.aggregations.TermsAggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.*;

import java.util.List;
import java.util.function.UnaryOperator;

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

    public static Query buildRangeQuery(String field, UnaryOperator<NumberRangeQuery.Builder> function) {
        NumberRangeQuery numberRangeQuery = NumberRangeQuery.of(builder -> function.apply(builder.field(field)));
        RangeQuery rangeQuery = RangeQuery.of(builder -> builder.number(numberRangeQuery));
        return Query.of(builder -> builder.range(rangeQuery));
    }

    public static Query buildMultiMatchQuery(List<String> fields, String searchTerm) {
        var multiMatchQuery = MultiMatchQuery.of(builder -> builder
                .query(searchTerm)
                .fields(fields)
                .fuzziness(Constants.Fuzzy.LEVEL)//issue with crossfields
                .prefixLength(Constants.Fuzzy.PREFIX_LENGTH)
                //.type(TextQueryType.CrossFields)
                .type(TextQueryType.BestFields)
                //.operator(Operator.And)
                // // Amazon-style logic:
                //            // - 2 words or fewer? 100% must match.
                //            // - 3 to 5 words? All words minus 1 must match.
                //            // - More than 5 words? 70% of the words must match.
                .minimumShouldMatch("2<-1 5<70%"));

        return Query.of(builder -> builder.multiMatch(multiMatchQuery));
    }

    public static Aggregation buildTermsAggregation(String field){
        var termsAggregation = TermsAggregation.of(builder -> builder.field(field).size(10));
        return Aggregation.of(builder -> builder.terms(termsAggregation));
    }
}
