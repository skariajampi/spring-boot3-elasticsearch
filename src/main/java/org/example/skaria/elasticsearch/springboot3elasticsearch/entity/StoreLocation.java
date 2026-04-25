package org.example.skaria.elasticsearch.springboot3elasticsearch.entity;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@Data
class StoreLocation {
    @Field(name = "store_id", type = FieldType.Keyword)
    private String storeId;

    @Field(type = FieldType.Text)
    private String name;
    private GeoPoint location;

    @Field(name = "store_in_stock", type = FieldType.Boolean)
    private Boolean storeInStock;
}