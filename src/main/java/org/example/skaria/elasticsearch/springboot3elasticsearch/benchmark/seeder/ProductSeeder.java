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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Profile("benchmark")
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductSeeder {

    private final ProductGenerator productGenerator;
    private final ElasticsearchOperations elasticsearchOperations;
    private final AtomicBoolean running = new AtomicBoolean(false);
    // Create a dedicated executor service.
    // Virtual threads (Java 21+) are perfect for heavy blocking network I/O like ES indexing.
    private final ExecutorService seederExecutor = Executors.newVirtualThreadPerTaskExecutor();

    public CompletableFuture<Void> seed(String theDatasetVersion, int total, int batchSize) {

        if (!running.compareAndSet(false, true)) {

            log.warn("Seed rejected. Another seed is already running.");

            return CompletableFuture.failedFuture(new IllegalStateException("Seed already running"));

        }

        // Track overall successful indexing counts across threads safely
        AtomicInteger successfulCount = new AtomicInteger(0);
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        // Capture the start time for throughput benchmarks
        long startTime = System.currentTimeMillis();
        return CompletableFuture.supplyAsync(() -> {
            log.info("Started seeding {} products for data set version {}", total, theDatasetVersion);
            List<IndexQuery> batch = new ArrayList<>();

            try {

                for (int i = 0; i < total; i++) {
                    ProductDocument productDocument = productGenerator.generate(i, theDatasetVersion);
                    batch.add(new IndexQueryBuilder()
                            .withId(productDocument.getProductId())
                            .withObject(productDocument)
                            .build());

                    if (batch.size() >= batchSize) {
                        // Snapshot the batch and ship it off to a background thread task
                        List<IndexQuery> batchToFlush = batch;
                        futures.add(submitBatch(batchToFlush, successfulCount));

                        // Open a fresh list for the next batch immediately without waiting
                        batch = new ArrayList<>(batchSize);
                    }
                }



                // Flush out any remaining trailing documents
                if (!batch.isEmpty()) {
                    futures.add(submitBatch(batch, successfulCount));
                }

                // Wait for ALL concurrent background flush operations to finish completely
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

                long duration = System.currentTimeMillis() - startTime;

                log.info("Finished seeding {}/{} products for data set version {}", successfulCount, total, theDatasetVersion);
                //return CompletableFuture.completedFuture(null);
                if (successfulCount.get() != total) {
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
        }, seederExecutor);//Runs the entire generation pipeline context on our dedicated executor
    }

    private CompletableFuture<Void> submitBatch(List<IndexQuery> batchToFlush, AtomicInteger successfulCount) {
        // Run the blocking Elasticsearch HTTP rest client request inside a background thread pool task
        return CompletableFuture.runAsync(() -> {
            try {
                int flushed = flush(batchToFlush);
                int currentTotal = successfulCount.addAndGet(flushed);
                log.info("Progress update: Async bulk write complete. Total synced: {}", currentTotal);
            } catch (Exception e) {
                log.error("Failed to asynchronously flush bulk batch array payload downstream to Elasticsearch cluster", e);
                throw e;
            }
        }, seederExecutor);
    }

    private int flush(List<IndexQuery> batch) {

        List<IndexedObjectInformation> products = elasticsearchOperations.bulkIndex(batch, IndexCoordinates.of("products"));
        return products.size();
    }

    public boolean isRunning() {
        return running.get();
    }
}
