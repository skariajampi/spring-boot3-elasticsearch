package org.example.skaria.elasticsearch.springboot3elasticsearch.benchmark.seeder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.skaria.elasticsearch.springboot3elasticsearch.benchmark.generator.ProductGenerator;
import org.example.skaria.elasticsearch.springboot3elasticsearch.entity.ProductDocument;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexedObjectInformation;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@Profile("benchmark")
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductSeeder {

    private final ProductGenerator productGenerator;
    private final ElasticsearchOperations elasticsearchOperations;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public CompletableFuture<Void> seed(String theDatasetVersion, int total, int batchSize) {

        if (!running.compareAndSet(false, true)) {

            log.warn(
                    "Seed rejected. Another seed is already running."
            );

            throw new IllegalStateException(
                    "Seed already running"
            );
        }

        return CompletableFuture.supplyAsync(() -> {
            log.info("Started seeding {} products for data set version {}", total, theDatasetVersion);
            List<IndexQuery> batch = new ArrayList<>();
            int successfulCount = 0;
            try {

                for (int i = 0; i < total; i++) {
                    ProductDocument productDocument = productGenerator.generate(i, theDatasetVersion);
                    batch.add(new IndexQueryBuilder()
                            .withId(productDocument.getProductId())
                            .withObject(productDocument)
                            .build());

                    if (batch.size() >= batchSize) {
                        int flushed = flush(batch);
                        successfulCount += flushed;
                        batch.clear();
                        log.info("Progress: {}/{} products flushed", successfulCount, total);
                    }
                }



                if (!batch.isEmpty()) {
                    int flushed = flush(batch);
                    successfulCount += flushed;
                    batch.clear();
                }

                log.info("Finished seeding {}/{} products for data set version {}", successfulCount, total, theDatasetVersion);
                //return CompletableFuture.completedFuture(null);
                if (successfulCount != total) {
                    throw new RuntimeException(String.format(
                            "Only %d out of %d documents were indexed", successfulCount, total));
                }
                return null;
            } catch (Exception e) {
                log.error("Error seeding products for data set version {}", theDatasetVersion, e);
                //return CompletableFuture.failedFuture(e);
                throw new RuntimeException(e);
            } finally {
                running.set(false);
                log.info("Seeder unlocked");
            }
        });
    }

    private int flush(List<IndexQuery> batch) {

        List<IndexedObjectInformation> products = elasticsearchOperations.bulkIndex(batch, IndexCoordinates.of("products"));
        return products.size();
    }

    public boolean isRunning() {
        return running.get();
    }
}
