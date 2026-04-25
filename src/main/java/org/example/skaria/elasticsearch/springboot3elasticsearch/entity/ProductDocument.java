package org.example.skaria.elasticsearch.springboot3elasticsearch.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import org.springframework.data.elasticsearch.core.suggest.Completion;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Document(indexName = "products")
@Setting(settingPath = "products-index-settings.json") // Point to your analyzer definitions
public class ProductDocument {

    @Id
    @Field(name = "product_id", type = FieldType.Keyword)
    private String productId;

    @Field(type = FieldType.Keyword)
    private String sku;

    @MultiField(
        mainField = @Field(type = FieldType.Text),
        otherFields = {
            @InnerField(suffix = "keyword", type = FieldType.Keyword, ignoreAbove = 256),
            @InnerField(suffix = "autocomplete", type = FieldType.Text, analyzer = "autocomplete_analyzer", searchAnalyzer = "autocomplete_search_analyzer")
        }
    )
    private String title;

    @MultiField(
        mainField = @Field(type = FieldType.Text, analyzer = "standard"),
        otherFields = @InnerField(suffix = "english", type = FieldType.Text, analyzer = "english")
    )
    private String description;

    @MultiField(
        mainField = @Field(type = FieldType.Text),
        otherFields = {
            @InnerField(suffix = "keyword", type = FieldType.Keyword),
            @InnerField(suffix = "autocomplete", type = FieldType.Text, analyzer = "autocomplete_analyzer", searchAnalyzer = "autocomplete_search_analyzer")
        }
    )
    private String brand;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Keyword)
    private String category;

    @MultiField(
        mainField = @Field(type = FieldType.Text),
        otherFields = {
            @InnerField(suffix = "keyword", type = FieldType.Keyword, ignoreAbove = 512),
            @InnerField(suffix = "hierarchy", type = FieldType.Text, analyzer = "path_hierarchy_analyzer")
        }
    )
    private String categoryPath;

    @Field(type = FieldType.Scaled_Float, scalingFactor = 100)
    private BigDecimal price;

    @Field(name = "compare_at_price", type = FieldType.Scaled_Float, scalingFactor = 100)
    private BigDecimal compareAtPrice;

    @Field(name = "price_range", type = FieldType.Keyword)
    private String priceRange;

    @Field(type = FieldType.Keyword)
    private String currency;

    @Field(type = FieldType.Integer)
    private Integer inventory;

    @Field(name = "low_stock", type = FieldType.Boolean)
    private Boolean lowStock;

    @Field(type = FieldType.Nested)
    private List<Image> images;

    @Field(type = FieldType.Nested)
    private List<Attribute> attributes;

    @Field(type = FieldType.Nested)
    private List<ProductVariant> variants;

    private Ratings ratings;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private Instant createdAt;

    @Field(type = FieldType.Float)
    private Float boostScore;

    // Amazon-style Completion Suggestion
    @CompletionField(maxInputLength = 100)
    private Completion suggest;

    private GeoInfo geo;

    //inventory
    @Field(name = "inventory_threshold", type = FieldType.Integer)
    private Integer inventoryThreshold;

    //metrics
    @Field(name = "view_count", type = FieldType.Integer)
    private Integer viewCount;

    @Field(name = "reviews_count", type = FieldType.Integer)
    private Integer reviewsCount;

    @Field(name = "purchase_count", type = FieldType.Integer)
    private Integer purchaseCount;

    @Field(name = "click_through_rate", type = FieldType.Float)
    private Float clickThroughRate;

    //flags
    @Field(name = "is_featured", type = FieldType.Boolean)
    private Boolean isFeatured;

    @Field(name = "is_bestseller", type = FieldType.Boolean)
    private Boolean isBestseller;

    @Field(name = "is_new", type = FieldType.Boolean)
    private Boolean isNew;

    //metadata
    @Field(type = FieldType.Keyword)
    private List<String> tags;

    @Field(name = "updated_at", type = FieldType.Date, format = DateFormat.date_time)
    private Instant updatedAt;

    //ranking
    @Field(name = "search_boost_score", type = FieldType.Rank_Feature)
    private Float searchBoostScore;
}