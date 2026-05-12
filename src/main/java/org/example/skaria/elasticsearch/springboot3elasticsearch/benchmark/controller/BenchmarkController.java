package org.example.skaria.elasticsearch.springboot3elasticsearch.benchmark.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.skaria.elasticsearch.springboot3elasticsearch.benchmark.seeder.ProductSeeder;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@Profile("benchmark")
@RestController
@RequestMapping("/benchmark")
@RequiredArgsConstructor
@Slf4j
public class BenchmarkController {

    private final ProductSeeder seeder;

    @PostMapping("/seed")
    public ResponseEntity<String> seed(@RequestBody SeedRequest seedRequest) {
        if (seeder.isRunning()) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Seed already running");
        }
        String theDatasetVersion = "dataset-v" + seedRequest.datasetVersion();
        log.info("Seeding {} products for data set version {}", seedRequest.total(), theDatasetVersion);
        CompletableFuture<Void> seed = seeder.seed(theDatasetVersion, seedRequest.total(), seedRequest.batchSize());


        return ResponseEntity.accepted().build();
    }
}