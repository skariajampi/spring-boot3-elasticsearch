package org.example.skaria.elasticsearch.springboot3elasticsearch.query;

import org.example.skaria.elasticsearch.springboot3elasticsearch.entity.ProductDocument;

import java.util.List;
import java.util.Objects;

import static org.example.skaria.elasticsearch.springboot3elasticsearch.query.ElasticSearchUtil.*;

public class QueryRules {

    private static final String BOOST_FIELD_FORMAT = "%s^%f";

    private QueryRules(){}
    public static final QueryRule RATING_QUERY = QueryRule.of(
            srp -> Objects.nonNull(srp.getAvgRating()),
            srp -> buildRangeQuery(ProductDocument.Fields.Ratings.AVERAGE.getValue(),
                    builder -> builder.gte(srp.getAvgRating()))
            );

    private static final List<String> SEARCH_BOOST_FIELDS = List.of(
            boostField(ProductDocument.Fields.Title.MAIN.getValue(), 2.0f),
            boostField(ProductDocument.Fields.Description.ENGLISH.getValue(), 1.5f)
    );

    public static final QueryRule SEARCH_QUERY = QueryRule.of(
            srp -> Objects.nonNull(srp.getQuery()),  // can also use Predicates.isTrue() if it is true always
            srp -> buildMultiMatchQuery(SEARCH_BOOST_FIELDS, srp.getQuery())
    );

    public static final QueryRule BRAND_QUERY = QueryRule.of(
            srp -> Objects.nonNull(srp.getBrand()),
            srp -> buildTermQuery(ProductDocument.Fields.Brand.KEYWORD.getValue(), srp.getBrand(), 1.0f)
    );

    public static final QueryRule CATEGORY_QUERY = QueryRule.of(
            srp -> Objects.nonNull(srp.getQuery()),
            srp -> buildTermQuery(ProductDocument.Fields.Core.CATEGORY.getValue(), srp.getCategory(), 1.0f)
    );

    private static String boostField(String field, float boost){
        return BOOST_FIELD_FORMAT.formatted(field, boost);
    }

}
