package org.example.skaria.elasticsearch.springboot3elasticsearch.entity;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.util.Map;

@Data
public class CollectionPoint {
    @Field(type = FieldType.Text)
    private String name;

    private GeoPoint location;

    @Field(type = FieldType.Text)
    private String address;

    /**
     * "enabled": false in your mapping means this object is stored but not indexed.
     * We use Map<String, Object> to flexibly store the opening hours structure
     * (e.g., {"monday": "9:00-18:00", ...}) without searching on it.
     */
    @Field(type = FieldType.Object, enabled = false)
    private Map<String, Object> openingHours;
}