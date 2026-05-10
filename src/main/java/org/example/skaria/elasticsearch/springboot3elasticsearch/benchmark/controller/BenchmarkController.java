package org.example.skaria.elasticsearch.springboot3elasticsearch.benchmark.controller;

import lombok.RequiredArgsConstructor;
import org.example.skaria.elasticsearch.springboot3elasticsearch.benchmark.seeder.ProductSeeder;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("benchmark")
@RestController
@RequestMapping("/benchmark")
@RequiredArgsConstructor
public class BenchmarkController {

    private final ProductSeeder seeder;

    @PostMapping("/seed/{total}")
    public void seed(@PathVariable int total) {
        seeder.seed(total);
    }
}