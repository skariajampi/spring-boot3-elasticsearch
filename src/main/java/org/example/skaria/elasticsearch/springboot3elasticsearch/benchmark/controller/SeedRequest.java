package org.example.skaria.elasticsearch.springboot3elasticsearch.benchmark.controller;

public record SeedRequest(
        String datasetVersion,
        int total,
        int batchSize
) {
}
