package org.example.skaria.elasticsearch.springboot3elasticsearch.benchmark.generator;

import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import org.example.skaria.elasticsearch.springboot3elasticsearch.entity.ProductDocument;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Profile("benchmark")
@Component
@RequiredArgsConstructor
public class ProductGenerator {

    private final DataSetLoader data;
    private final Faker faker = new Faker();

    public ProductDocument generate(long i, String datasetVersion) {

        String brand = random(data.getBrands());
        String category = randomCategory();

        String adjective = random(data.getAdjectives());

        String model = faker.bothify("??-###");

        String title = String.format(
                "%s %s %s %s",
                brand,
                adjective,
                category,
                model
        );

        String description = faker.lorem().sentences(3).stream()
                .reduce("", (a, b) -> a + " " + b);

        return ProductDocument.builder()
                .productId(UUID.randomUUID().toString())
                .sku("SKU-" + datasetVersion + "-" + i)
                .brand(brand)
                .category(category)
                .title(title)
                .description(description)
                .price(generatePrice(category))
                .tags(generateTags(brand, category))
                .createdAt(Instant.now())
                .build();
    }

    private String random(List<String> list) {
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    private String randomCategory() {
        List<String> list = data.getCategories().values()
                .stream()
                .flatMap(listOfStrings -> listOfStrings.stream())
                .toList();

        ArrayList<String> flattenedCategories = new ArrayList<>(list);
        return random(flattenedCategories);
    }

    private BigDecimal generatePrice(String category) {
        return switch (category) {
            case "Laptops" -> BigDecimal.valueOf(500 + faker.number().numberBetween(0, 2000));
            case "Phones" -> BigDecimal.valueOf(200 + faker.number().numberBetween(0, 1200));
            case "Tablets" -> BigDecimal.valueOf(200 + faker.number().numberBetween(0, 1500));
            case "TVs" -> BigDecimal.valueOf(300 + faker.number().numberBetween(0, 10000));
            case "Shoes" -> BigDecimal.valueOf(30 + faker.number().numberBetween(0, 200));
            case "Jackets" -> BigDecimal.valueOf(30 + faker.number().numberBetween(0, 200));
            case "T-Shirts" -> BigDecimal.valueOf(10 + faker.number().numberBetween(0, 200));
            case "Kitchen Appliances" -> BigDecimal.valueOf(50 + faker.number().numberBetween(0, 2000));
            case "Furniture" -> BigDecimal.valueOf(300 + faker.number().numberBetween(0, 10000));
            case "Lighting" -> BigDecimal.valueOf(30 + faker.number().numberBetween(0, 2000));
            default -> BigDecimal.valueOf(10 + faker.number().numberBetween(0, 500));
        };
    }

    private List<String> generateTags(String brand, String category) {
        return List.of(
                brand.toLowerCase(),
                category.toLowerCase(),
                faker.commerce().material(),
                faker.bool().bool() ? "bestseller" : "standard"
        );
    }
}
