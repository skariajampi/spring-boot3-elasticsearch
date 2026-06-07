package org.example.skaria.elasticsearch.springboot3elasticsearch.service;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsAggregate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.skaria.elasticsearch.springboot3elasticsearch.dto.*;
import org.example.skaria.elasticsearch.springboot3elasticsearch.entity.ProductDocument;
import org.example.skaria.elasticsearch.springboot3elasticsearch.mapper.ProductMapper;
import org.example.skaria.elasticsearch.springboot3elasticsearch.query.Constants;
import org.example.skaria.elasticsearch.springboot3elasticsearch.query.NativeQueryBuilder;
import org.example.skaria.elasticsearch.springboot3elasticsearch.repository.ProductRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchAggregation;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductSearchServiceImpl implements ProductSearchService{
    private final ProductMapper productMapper;

    private final ProductRepository productRepository; // Your ElasticsearchRepository
    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public SearchResponseDTO searchProducts(SearchProductsRequestDTO searchProductsRequestDTO) {
        log.info("Search request: {}", searchProductsRequestDTO);
        NativeQuery searchQuery = NativeQueryBuilder.buildSearchQuery(searchProductsRequestDTO);
        log.info("Searching for products with query: {}", searchQuery.getQuery().toString());
        SearchHits<ProductDocument> searchHits = this.elasticsearchOperations.search(searchQuery, ProductDocument.class, Constants.Index.PRODUCTS);
        return buildResponse(searchProductsRequestDTO, searchHits);
    }

    @Override
    public List<String> getAutocompleteSuggestions(String partialQuery) {
        return List.of();
    }


    public Optional<ProductDocument> findById(String id) {
        return productRepository.findById(id);
    }

    private SearchResponseDTO buildResponse(SearchProductsRequestDTO searchProductsRequestDTO, SearchHits<ProductDocument> searchHits) {
        List<ProductDTO> results = searchHits.getSearchHits()
                .stream()
                .map(SearchHit::getContent)
                .map(productMapper::toDto)
                .toList();
        log.info("Found {} products", results.size());

        SearchPage<ProductDocument> productDocumentSearchPage =
                SearchHitSupport.searchPageFor(searchHits, PageRequest.of(searchProductsRequestDTO.getPage(), searchProductsRequestDTO.getSize()));
        var pagination = new PaginationDTO(
                productDocumentSearchPage.getNumber(),
                productDocumentSearchPage.getNumberOfElements(),
                productDocumentSearchPage.getTotalElements(),
                productDocumentSearchPage.getTotalPages()
        );
        List<FacetDTO> facets = buildFacets((List<ElasticsearchAggregation>) searchHits.getAggregations().aggregations());
        return new SearchResponseDTO(
                results,
                facets,
                pagination,
                searchHits.getExecutionDuration().toMillis()
        );
    }

    private List<FacetDTO> buildFacets(List<ElasticsearchAggregation> aggregations) {
        Map<String, Aggregate> aggregateMap = aggregations.stream()
                .map(ElasticsearchAggregation::aggregation)
                .collect(Collectors.toMap(
                        a -> a.getName(),
                        a -> a.getAggregate()
                ));
        return List.of(
                buildFacet(ProductDocument.Fields.Aggregation.BRAND.getValue(), aggregateMap.get(ProductDocument.Fields.Aggregation.BRAND.getValue()).sterms())
        );
    }

    private FacetDTO buildFacet(String name, StringTermsAggregate stringTermsAggregate) {
        List<FacetItemDTO> facetItemDTOList = stringTermsAggregate.buckets()
                .array()
                .stream()
                .map(b -> new FacetItemDTO(b.key().stringValue(), b.docCount()))
                .toList();
        return new FacetDTO(name, facetItemDTOList);
    }
}