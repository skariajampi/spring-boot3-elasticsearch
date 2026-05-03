package org.example.skaria.elasticsearch.springboot3elasticsearch.mapper;

   // Your manual ES Entity

import org.example.skaria.elasticsearch.springboot3elasticsearch.entity.ProductDocument;
import org.example.skaria.elasticsearch.springboot3elasticsearch.model.ProductDTO;
import org.mapstruct.Mapper;
import org.springframework.data.elasticsearch.core.SearchHit;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    // 1. Map a single document to the API response
    // MapStruct automatically handles matching field names (id, title, price, etc.)
    //@Mapping(source = "id", target = "productId") // If your Entity uses 'id' but YAML uses 'productId'
    //@Mapping(source = "avgRating", target = "ratings.average")
    //@Mapping(source = "reviewCount", target = "ratings.count")
    ProductDTO toDto(ProductDocument entity);

    // 2. Map a list of documents
    List<ProductDTO> toDtoList(List<ProductDocument> entities);

    // 3. Senior Dev Tip: Map directly from Elasticsearch SearchHit
    // This allows you to map metadata like 'score' if needed later
    default ProductDTO fromSearchHit(SearchHit<ProductDocument> hit) {
        return toDto(hit.getContent());
    }

    // MapStruct will use this to convert the nested Map within the Ratings object
    default Map<String, Integer> mapDistribution(Map<String, Object> source) {
        if (source == null) return null;

        return source.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> {
                            if (e.getValue() instanceof Number n) return n.intValue();
                            return 0; // or handle null/strings
                        },
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }

    /**
     * MapStruct will automatically use this whenever it needs to
     * convert an Instant to an OffsetDateTime.
     */
    default OffsetDateTime map(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atOffset(ZoneOffset.UTC);
    }

    default Instant map(OffsetDateTime offsetDateTime) {
        if (offsetDateTime == null) {
            return null;
        }
        return offsetDateTime.toInstant();
    }

    ProductDocument toDocument(ProductDTO productDTO);
}