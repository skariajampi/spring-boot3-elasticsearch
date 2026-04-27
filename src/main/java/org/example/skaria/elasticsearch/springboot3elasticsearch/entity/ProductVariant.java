package org.example.skaria.elasticsearch.springboot3elasticsearch.entity;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductVariant {
    @Field(name = "variant_id", type = FieldType.Keyword)
    private String variantId;
    
    @Field(type = FieldType.Keyword)
    private String sku;

    @Field(type = FieldType.Scaled_Float, scalingFactor = 100)
    private BigDecimal price;

    @Field(type = FieldType.Integer)
    private Integer inventory;

    @Field(type = FieldType.Nested)
    private List<Attribute> attributes;

    @Field(name = "compare_at_price", type = FieldType.Scaled_Float, scalingFactor = 100)
    private BigDecimal compareAtPrice;
}