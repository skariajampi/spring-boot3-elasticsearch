package org.example.skaria.elasticsearch.springboot3elasticsearch.benchmark.seeder;

import lombok.RequiredArgsConstructor;
import org.example.skaria.elasticsearch.springboot3elasticsearch.benchmark.generator.ProductGenerator;
import org.example.skaria.elasticsearch.springboot3elasticsearch.entity.ProductDocument;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductSeeder {

    private final ProductGenerator productGenerator;
    private final ElasticsearchOperations elasticsearchOperations;

    public void seed(int total) {

        List<IndexQuery> batch = new ArrayList<>();
        for(int i = 0; i < total; i++){
            ProductDocument productDocument = productGenerator.generate(i);
            batch.add(new IndexQueryBuilder()
                    .withId(productDocument.getProductId())
                    .withObject(productDocument)
                    .build());
        }

        if(batch.size() == 1000){
            flush(batch);
        }

        if(!batch.isEmpty()){
            flush(batch);
        }
    }

    private void flush(List<IndexQuery> batch) {

        elasticsearchOperations.bulkIndex(batch, IndexCoordinates.of("products"));
    }
}
