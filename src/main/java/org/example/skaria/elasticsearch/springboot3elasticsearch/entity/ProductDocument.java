package org.example.skaria.elasticsearch.springboot3elasticsearch.entity;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import org.springframework.data.elasticsearch.core.suggest.Completion;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Document(indexName = "products")
@Setting(settingPath = "elasticsearch/products-index-settings.json") // Point to your analyzer definitions
@Builder
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

    @Field(type = FieldType.Nested)
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

    public static class Fields {
        @Getter
        public enum Core {
            PRODUCT_ID("product_id"),
            SKU("sku"),
            STATUS("status"),
            CATEGORY("category"),
            CATEGORY_PATH("category_path"),
            PRICE("price"),
            COMPARE_AT_PRICE("compare_at_price"),
            PRICE_RANGE("price_range"),
            CURRENCY("currency"),
            INVENTORY("inventory"),
            LOW_STOCK("low_stock"),
            IMAGES("images"),
            ATTRIBUTES("attributes"),
            VARIANTS("variants"),
            CREATED_AT("created_at"),
            BOOST_SCORE("boost_score"),
            SUGGEST("suggest"),
            GEO("geo"),
            INVENTORY_THRESHOLD("inventory_threshold"),
            VIEW_COUNT("view_count"),
            REVIEWS_COUNT("reviews_count"),
            PURCHASE_COUNT("purchase_count"),
            CLICK_THROUGH_RATE("click_through_rate"),
            IS_FEATURED("is_featured"),
            IS_BESTSELLER("is_bestseller"),
            IS_NEW("is_new"),
            TAGS("tags"),
            UPDATED_AT("updated_at"),
            SEARCH_BOOST_SCORE("search_boost_score"),
            SEARCH_BOOST_SCORE_FIELD("search_boost_score"),
            SEARCH_BOOST_SCORE_TYPE("rank_feature"),
            SEARCH_BOOST_SCORE_VALUE("1.0"),
            SEARCH_BOOST_SCORE_BOOST("1.0"),
            SEARCH_BOOST_SCORE_MULTIPLIER("1.0");

            private final String value;

            Core(String value) {
                this.value = value;
            }

            public String getValue() {
                return value;
            }

            @Override
            public String toString() {
                return value;
            }
        }

        @Getter
        public enum Title {
            MAIN("title"),
            KEYWORD("title.keyword"),
            AUTOCOMPLETE("title.autocomplete");

            private String value;

            Title(String value) {
                this.value = value;
            }

            @Override
            public String toString() {
                return value;
            }
        }

        @Getter
        public enum Description {
            MAIN("description"),
            ENGLISH("description.english");

            private String value;

            Description(String value) {
                this.value = value;
            }

            @Override
            public String toString() {
                return value;
            }
        }

        @Getter
        public enum Brand {
            MAIN("brand"),
            KEYWORD("brand.keyword"),
            AUTOCOMPLETE("brand.autocomplete");

            private String value;

            Brand(String value) {
                this.value = value;
            }

            @Override
            public String toString() {
                return value;
            }

        }

        @Getter
        public enum Ratings {
            MAIN("ratings"),
            AVERAGE("ratings.average"),
            COUNT("ratings.count");

            private String value;

            Ratings(String value) {
                this.value = value;
            }

            @Override
            public String toString() {
                return value;
            }
        }

        @Getter
        public enum Aggregation {
            BRAND("brand-terms-aggregate"),
            CATEGORY("category-terms-aggregate"),
            PRICE_RANGE("price-range-terms-aggregate");

            private String value;

            Aggregation(String value) {
                this.value = value;
            }

            @Override
            public String toString() {
                return value;
            }

        }

    }
}