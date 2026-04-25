package org.example.skaria.elasticsearch.springboot3elasticsearch.entity;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;

@Data
class Attribute {
    @Field(type = FieldType.Keyword)
    private String name;

    @MultiField(
        mainField = @Field(type = FieldType.Keyword),
        otherFields = @InnerField(suffix = "text", type = FieldType.Text, analyzer = "standard")
    )
    private String value;

    private Boolean filterable;
}

