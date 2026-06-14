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
import java.util.concurrent.Semaphore;
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
    // This pool is strictly reserved for network HTTP requests to Elasticsearch
    private final ExecutorService elasticsearchWorkerPool = Executors.newVirtualThreadPerTaskExecutor();

    // A single dedicated platform thread just for generating data so it never starves the pool
    private final ExecutorService generatorThread = Executors.newSingleThreadExecutor();
    private static final int MAX_CONCURRENT_BATCHES = 4;
    private final Semaphore backpressurePermits = new Semaphore(MAX_CONCURRENT_BATCHES);

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
            List<IndexQuery> currentBatch = new ArrayList<>();

            try {

                for (int i = 0; i < total; i++) {
                    ProductDocument productDocument = productGenerator.generate(i, theDatasetVersion);
                    currentBatch.add(new IndexQueryBuilder()
                            .withId(productDocument.getProductId())
                            .withObject(productDocument)
                            .build());

                    if (currentBatch.size() >= batchSize) {
                        // Snapshot the currentBatch and ship it off to a background thread task
                        List<IndexQuery> batchToFlush = currentBatch;
                        // ACQUIRE PERMIT: Blocks generator thread if MAX_CONCURRENT_BATCHES are currently in flight
                        backpressurePermits.acquire();

                        futures.add(submitBatch(batchToFlush, successfulCount));

                        // Open a fresh list for the next currentBatch immediately without waiting
                        currentBatch = new ArrayList<>(batchSize);
                    }
                }



                // Flush out any remaining trailing documents
                if (!currentBatch.isEmpty()) {
                    // ACQUIRE PERMIT: Blocks generator thread if MAX_CONCURRENT_BATCHES are currently in flight
                    backpressurePermits.acquire();
                    futures.add(submitBatch(currentBatch, successfulCount));
                }

                // Wait for ALL concurrent background flush operations to finish completely
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

                long duration = System.currentTimeMillis() - startTime;

                log.info("Finished seeding {}/{} products in {} ms for data set version {}", successfulCount.get(), total, duration, theDatasetVersion);
                //return CompletableFuture.completedFuture(null);
                if (successfulCount.get() != total) {
                    throw new RuntimeException(String.format(
                            "Only %d out of %d documents were indexed", successfulCount, total));
                }
                return null;
            }
            catch (InterruptedException e) {
                log.error("Seeder pipeline interrupted", e);
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
            catch (Exception e) {
                log.error("Error seeding products for data set version {}", theDatasetVersion, e);
                //return CompletableFuture.failedFuture(e);
                throw new RuntimeException(e);
            } finally {
                // Ensure we reset permits back to maximum on completion or crash
                backpressurePermits.drainPermits();
                backpressurePermits.release(MAX_CONCURRENT_BATCHES);
                running.set(false);
                log.info("Seeder unlocked");
            }
        }, generatorThread);// RUN GENERATOR ON ITS OWN DEDICATED THREAD
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
            }finally {
                // GUARANTEED RELEASE: Runs no matter what happens inside the execution block
                backpressurePermits.release();
            }
        }, elasticsearchWorkerPool);// RUN FLUSH TASKS ON THE HTTP WORKER POOL
    }

    private int flush(List<IndexQuery> batch) {

        List<IndexedObjectInformation> products = elasticsearchOperations.bulkIndex(batch, IndexCoordinates.of("products"));
        return products.size();
    }

    public boolean isRunning() {
        return running.get();
    }
}
