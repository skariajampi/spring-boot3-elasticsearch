package org.example.skaria.elasticsearch.springboot3elasticsearch.query;

import org.example.skaria.elasticsearch.springboot3elasticsearch.entity.ProductDocument;

import java.util.Objects;


import static org.example.skaria.elasticsearch.springboot3elasticsearch.query.ElasticSearchUtil.buildTermQuery;

public class QueryRules {

    private QueryRules(){}
    public static final QueryRule CATEGORY_QUERY = QueryRule.of(
            srp -> Objects.nonNull(srp.getCategory()),
            srp -> buildTermQuery(ProductDocument.Fields.Core.CATEGORY.getValue(), srp.getCategory(), 1.0f)
            );

    public static final QueryRule TITLE_QUERY = QueryRule.of(
            srp -> Objects.nonNull(srp.getTitle()),
            srp -> buildTermQuery(ProductDocument.Fields.Title.KEYWORD.getValue(), srp.getTitle(), 1.0f)
    );

    public static final QueryRule BRAND_QUERY = QueryRule.of(
            srp -> Objects.nonNull(srp.getBrand()),
            srp -> buildTermQuery(ProductDocument.Fields.Brand.KEYWORD.getValue(), srp.getBrand(), 1.0f)
    );

}
