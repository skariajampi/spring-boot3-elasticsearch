package org.example.skaria.elasticsearch.springboot3elasticsearch.entity;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
class Image {
    @Field(type = FieldType.Keyword)
    private String url;
    @Field(name = "is_primary", type = FieldType.Boolean)
    private Boolean isPrimary;

    @Field(name = "alt_text", type = FieldType.Text)
    private String altText;
}