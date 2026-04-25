package org.example.skaria.elasticsearch.springboot3elasticsearch.entity;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import java.util.Map;

@Data
public class Ratings {

    @Field(type = FieldType.Float)
    private Float average;

    @Field(type = FieldType.Integer)
    private Integer count;

    /**
     * "enabled": false means the content is not parsed or indexed.
     * In Spring Data, we map this as an Object type.
     * Using a Map<String, Object> is the most flexible way to handle
     * unindexed distribution data.
     * Data Type: I used Map<String, Object> because it allows you to store any arbitrary distribution
     * (e.g., {"1": 10, "2": 20...}) without needing a rigid Java class for it.
     * Since it isn't indexed anyway, a Map is usually the cleanest approach.
     * enabled: false?
     *
     * "Distribution" data. You likely want to show the star breakdown on a UI (retrieve it),
     * but you probably never need to ask Elasticsearch,
     * "Find all products where exactly 42 people gave a 3-star rating."
     * Disabling the index for this field saves disk space and improves indexing speed.
     */
    @Field(type = FieldType.Object, enabled = false)
    private Map<String, Object> distribution;
}
