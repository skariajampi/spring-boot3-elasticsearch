package org.example.skaria.elasticsearch.springboot3elasticsearch.entity;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.util.List;

@Data
public class GeoInfo {

    private GeoPoint warehouse;

    @Field(name = "delivery_radius_km", type = FieldType.Integer)
    private Integer deliveryRadiusKm;

    @MultiField(
            mainField = @Field(name = "delivery_zones", type = FieldType.Keyword),
            otherFields = @InnerField(suffix = "hierarchy", type = FieldType.Text, analyzer = "path_hierarchy_analyzer")
    )
    private String deliveryZones;

    @Field(name = "postal_codes_served", type = FieldType.Keyword, normalizer = "postal_code_normalizer")
    private List<String> postalCodesServed;

    @Field(type = FieldType.Nested)
    private List<StoreLocation> storeLocations;

    @Field(type = FieldType.Nested)
    private List<CollectionPoint> collectionPoints;

}


