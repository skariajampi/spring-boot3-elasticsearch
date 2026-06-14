package org.example.skaria.elasticsearch.springboot3elasticsearch.benchmark.generator;

import com.github.javafaker.Faker;
import lombok.RequiredArgsConstructor;
import org.example.skaria.elasticsearch.springboot3elasticsearch.entity.*;
import org.springframework.context.annotation.Profile;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.data.elasticsearch.core.suggest.Completion;

@Profile("benchmark")
@Component
@RequiredArgsConstructor
public class ProductGenerator {

    private static final List<String> UK_ZONES = List.of(
            "London", "South East", "South West", "East of England",
            "West Midlands", "East Midlands", "North West", "Yorkshire",
            "North East", "Scotland", "Wales", "Northern Ireland"
    );
    private final DataSetLoader data;
    private final Faker faker = new Faker();

    private static final List<String> CURRENCIES = List.of("USD", "EUR", "GBP", "JPY", "CAD");
    private static final List<String> STATUSES = List.of("active", "inactive", "discontinued", "coming_soon");
    private static final List<String> MATERIALS = List.of("leather", "plastic", "metal", "cotton", "polyester", "wood", "glass");
    private static final List<String> COLORS = List.of("Red", "Blue", "Green", "Black", "White", "Silver", "Gold");
    private static final List<String> SIZES = List.of("XS", "S", "M", "L", "XL", "XXL");
    private static final List<String> UK_AREAS = List.of("SW", "EC", "WC", "N", "E", "SE", "NW", "NE", "M", "B", "L", "G", "EH", "CF", "BS");
    // 1. Cache a pool of random words and street names at the class level
    private static final List<String> PRE_GENERATED_WORDS = List.of(
            "Hub", "Locker", "Store", "Express", "Station", "Depot", "Counter", "Kiosk"
    );
    private static final List<String> PRE_GENERATED_STREETS = List.of(
            "High Street", "Station Road", "Main Street", "London Road", "Church Street",
            "Park Lane", "Victoria Road", "Queens Road", "New Road", "Manchester Road"
    );

    // Pre-generated, high-quality descriptive components for full-text search indexing
    private static final List<String> DESC_HOOKS = List.of(
            "Experience the next level of performance with this engineered design.",
            "An exceptional blend of modern style and everyday functionality.",
            "Designed to seamlessly integrate into your daily routine and lifestyle.",
            "Discover unmatched reliability and premium build quality out of the box.",
            "The perfect solution for users seeking efficiency without compromising quality."
    );

    private static final List<String> DESC_BENEFITS = List.of(
            "Constructed using durable, sustainable materials for long-lasting life.",
            "Features an ergonomic setup that optimizes comfort during extended use.",
            "Engineered with cutting-edge technology to deliver maximum output.",
            "Boasts a sleek, minimalist aesthetic that complements any modern space.",
            "Includes advanced safety protections and highly intuitive user controls."
    );

    private static final List<String> DESC_CLOSINGS = List.of(
            "Ideal for professionals and casual enthusiasts alike.",
            "Backed by our standard comprehensive manufacturer warranty.",
            "An excellent addition to your collection or an amazing gift choice.",
            "Ships securely with an easy-to-follow setup instruction manual guide.",
            "Upgrade your setup today and experience the difference yourself."
    );

    private static final List<String> ADJECTIVES = List.of("Durable", "Ergonomic", "Eco-Friendly", "Premium", "Sleek", "Rustic", "Modern");

    // Pre-allocated immutable static structures to eliminate runtime heap allocation
    private static final Map<String, Object> OPENING_HOURS_OPEN = Map.of(
            "monday", "9:00-18:00", "tuesday", "9:00-18:00", "wednesday", "9:00-18:00",
            "thursday", "9:00-18:00", "friday", "9:00-18:00", "saturday", "9:00-17:00", "sunday", "10:00-16:00"
    );
    private static final Map<String, Object> OPENING_HOURS_CLOSED = Map.of(
            "monday", "9:00-18:00", "tuesday", "9:00-18:00", "wednesday", "9:00-18:00",
            "thursday", "9:00-18:00", "friday", "9:00-18:00", "saturday", "9:00-17:00", "sunday", "closed"
    );


    public ProductDocument generate(long i, String datasetVersion) {
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        String brand = random(data.getBrands());
        String category = randomCategory();
        String categoryPath = generateCategoryPath(category);
        String adjective = random(data.getAdjectives());
        // Fast alpha-numeric model name bypasses Faker.bothify()
        String model = "" + (char) rand.nextInt(65, 91) + (char) rand.nextInt(65, 91) + "-" + rand.nextInt(100, 1000);
        // Manual efficient compilation instead of String.format
        String title = brand + " " + adjective + " " + category + " " + model;
        // ⚡ NEW: Generate unique, searchable English descriptions on the fly
        String description = new StringBuilder(180) // Pre-size buffer to prevent internal array copy resizes
                .append(DESC_HOOKS.get(rand.nextInt(DESC_HOOKS.size())))
                .append(" ")
                .append(DESC_BENEFITS.get(rand.nextInt(DESC_BENEFITS.size())))
                .append(" ")
                .append(DESC_CLOSINGS.get(rand.nextInt(DESC_CLOSINGS.size())))
                .toString();

        BigDecimal price = generatePrice(category);
        BigDecimal compareAtPrice = randomBoolean(0.3) ? price.multiply(BigDecimal.valueOf(1.2 + ThreadLocalRandom.current().nextDouble(0.5))) : null;
        String priceRange = derivePriceRange(price);
        String currency = random(CURRENCIES);
        int inventory = ThreadLocalRandom.current().nextInt(0, 500);
        boolean lowStock = inventory < 10;
        int inventoryThreshold = 20;

        // Flags with realistic probabilities
        boolean isFeatured = randomBoolean(0.05);   // 5% featured
        boolean isBestseller = randomBoolean(0.1);  // 10% bestseller
        boolean isNew = randomBoolean(0.15);        // 15% new

        // Metrics
        int viewCount = ThreadLocalRandom.current().nextInt(0, 100_000);
        int reviewsCount = ThreadLocalRandom.current().nextInt(0, 500);
        int purchaseCount = ThreadLocalRandom.current().nextInt(0, 10_000);
        float clickThroughRate = (float) (ThreadLocalRandom.current().nextDouble(0.0, 0.25));

        // Boosts
        float boostScore = (float) ThreadLocalRandom.current().nextDouble(0.0, 2.0);
        float searchBoostScore = (float) ThreadLocalRandom.current().nextDouble(0.1, 5.0);

        // Timestamps: createdAt up to 2 years ago, updatedAt up to 30 days after createdAt (or now)
        Instant createdAt = Instant.now().minus(
                ThreadLocalRandom.current().nextInt(0, 730), ChronoUnit.DAYS);
        Instant updatedAt = createdAt.plus(ThreadLocalRandom.current().nextInt(0, 30), ChronoUnit.DAYS);

        return ProductDocument.builder()
                .productId(UUID.randomUUID().toString())
                .sku("SKU-" + datasetVersion + "-" + i)
                .brand(brand)
                .category(category)
                .categoryPath(categoryPath)
                .title(title)
                .description(description)
                .status(random(STATUSES))
                .price(price)
                .compareAtPrice(compareAtPrice)
                .priceRange(priceRange)
                .currency(currency)
                .inventory(inventory)
                .lowStock(lowStock)
                .inventoryThreshold(inventoryThreshold)
                .images(generateImages())
                .attributes(generateAttributes(ThreadLocalRandom.current(), brand))
                .variants(generateVariants())
                .ratings(generateRatings())
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .boostScore(boostScore)
                .searchBoostScore(searchBoostScore)
                .suggest(generateSuggest(title, brand, category))
                .geo(generateGeo())
                .tags(generateTags(brand, category, ThreadLocalRandom.current()))
                .viewCount(viewCount)
                .reviewsCount(reviewsCount)
                .purchaseCount(purchaseCount)
                .clickThroughRate(clickThroughRate)
                .isFeatured(isFeatured)
                .isBestseller(isBestseller)
                .isNew(isNew)
                .build();
    }

    // ----------------------------------------------------------------------
    // Helper methods for random selection, boolean, etc.
    // ----------------------------------------------------------------------
    private String random(List<String> list) {
        return list.get(ThreadLocalRandom.current().nextInt(list.size()));
    }

    private boolean randomBoolean(double probability) {
        return ThreadLocalRandom.current().nextDouble() < probability;
    }

    private String randomCategory() {
        return random(data.getFlattenedCategories());
    }

    // ----------------------------------------------------------------------
    // Field-specific generators
    // ----------------------------------------------------------------------
    private String generateCategoryPath(String leafCategory) {
        // Simple hierarchy: "Electronics > Laptops" etc.
        // You could extend with real path from DataSetLoader or fake it.
        String topLevel = switch (leafCategory) {
            case "Laptops", "Phones", "Tablets", "TVs" -> "Electronics";
            case "Shoes", "Jackets", "T-Shirts" -> "Clothing";
            case "Kitchen Appliances", "Lighting", "Furniture" -> "Home & Garden";
            default -> "Misc";
        };
        return topLevel + " > " + leafCategory;
    }

    private BigDecimal generatePrice(String category) {
        double min = switch (category) {
            case "Laptops" -> 500;
            case "Phones" -> 200;
            case "Tablets" -> 200;
            case "TVs" -> 300;
            case "Shoes" -> 30;
            case "Jackets" -> 30;
            case "T-Shirts" -> 10;
            case "Kitchen Appliances" -> 50;
            case "Furniture" -> 300;
            case "Lighting" -> 30;
            default -> 10;
        };
        double max = switch (category) {
            case "Laptops" -> 2500;
            case "Phones" -> 1400;
            case "Tablets" -> 1700;
            case "TVs" -> 10300;
            case "Shoes" -> 230;
            case "Jackets" -> 230;
            case "T-Shirts" -> 210;
            case "Kitchen Appliances" -> 2050;
            case "Furniture" -> 10300;
            case "Lighting" -> 2030;
            default -> 510;
        };
        double price = min + ThreadLocalRandom.current().nextDouble(max - min);
        return BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_UP);
    }

    private String derivePriceRange(BigDecimal price) {
        double p = price.doubleValue();
        if (p < 25) return "0-25";
        if (p < 50) return "25-50";
        if (p < 100) return "50-100";
        if (p < 250) return "100-250";
        if (p < 500) return "250-500";
        if (p < 1000) return "500-1000";
        if (p < 2500) return "1000-2500";
        return "2500+";
    }

    private List<String> generateTags(String brand, String category, ThreadLocalRandom rand) {
        List<String> tags = new ArrayList<>();
        tags.add(brand.toLowerCase());
        tags.add(category.toLowerCase());
        if (randomBoolean(0.7)) tags.add(MATERIALS.get(rand.nextInt(MATERIALS.size())));
        if (randomBoolean(0.3)) tags.add("bestseller");
        if (randomBoolean(0.2)) tags.add("limited");
        return tags;
    }

    private List<Image> generateImages() {
        int count = ThreadLocalRandom.current().nextInt(1, 4);
        List<Image> images = new ArrayList<>();
        for (int j = 0; j < count; j++) {
            images.add(Image.builder()
                    .url("https://example.com/img/" + ThreadLocalRandom.current().nextInt(1_000_000))
                    .altText("Image " + j)
                    .build());
        }
        return images;
    }

    private List<Attribute> generateAttributes(ThreadLocalRandom rand, String brand) {
        // Only 0-2 attributes, and only 30% of products
        if (randomBoolean(0.3)) {
            int count = rand.nextInt(0, 3);
            List<Attribute> attrs = new ArrayList<>();
            for (int j = 0; j < count; j++) {
                attrs.add(Attribute.builder()
                        .name(brand + " " + ADJECTIVES.get(rand.nextInt(ADJECTIVES.size())))
                        .value(MATERIALS.get(rand.nextInt(MATERIALS.size())))
                        .build());
            }
            return attrs;
        }
        return Collections.emptyList();
    }

    private List<ProductVariant> generateVariants() {
        int count = ThreadLocalRandom.current().nextInt(0, 4);
        List<ProductVariant> variants = new ArrayList<>();
        for (int j = 0; j < count; j++) {
            variants.add(ProductVariant.builder()
                    .sku(UUID.randomUUID().toString().substring(0, 8))
                    .price(BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(10, 500)))
                    .inventory(ThreadLocalRandom.current().nextInt(0, 100))
                    .build());
        }
        return variants;
    }

    private Ratings generateRatings() {
        double avg = 2.5 + ThreadLocalRandom.current().nextDouble(2.5); // 2.5 to 5.0
        int count = ThreadLocalRandom.current().nextInt(0, 1000);
        return Ratings.builder()
                .average(BigDecimal.valueOf(avg).setScale(1, RoundingMode.HALF_UP).floatValue())
                .count(count)
                .build();
    }

    private Completion generateSuggest(String title, String brand, String category) {
        List<String> inputs = new ArrayList<>();
        inputs.add(title);
        inputs.add(brand);
        inputs.add(category);
        // Add up to 3 words from title as separate suggestions
        String[] words = title.split(" ");
        for (int i = 0; i < Math.min(3, words.length); i++) {
            inputs.add(words[i]);
        }
        return new Completion(inputs.toArray(new String[0]));
    }

    private GeoInfo generateGeo() {
        ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
        // UK coordinates (mainland)
        double lat = 49.9 + threadLocalRandom.nextDouble(10.9);  // 49.9 – 60.8°N
        double lon = -8.6 + threadLocalRandom.nextDouble(10.4); // -8.6 – 1.8°E
        GeoPoint warehouse = new GeoPoint(lat, lon);

        int deliveryRadiusKm = threadLocalRandom.nextInt(5, 200);  // 5–200 km

        // UK regions instead of US zones
        String deliveryZones = random(UK_ZONES);

        int postcodeCount = threadLocalRandom.nextInt(1, 6);
        List<String> postcodesServed = new ArrayList<>(postcodeCount);
        for (int i = 0; i < postcodeCount; i++) {
            postcodesServed.add(generateUkPostcode());
        }

        // Generate 0-2 store locations
        List<StoreLocation> storeLocations = generateStoreLocations(ThreadLocalRandom.current());

        // Generate 0-3 collection points
        List<CollectionPoint> collectionPoints = generateCollectionPoints();

        return GeoInfo.builder()
                .warehouse(warehouse)
                .deliveryRadiusKm(deliveryRadiusKm)
                .deliveryZones(deliveryZones)
                .postalCodesServed(postcodesServed)
                .storeLocations(storeLocations)
                .collectionPoints(collectionPoints)
                .build();
    }

    private List<StoreLocation> generateStoreLocations(ThreadLocalRandom rand) {
        int count = ThreadLocalRandom.current().nextInt(0, 3); // 0–2 stores
        List<StoreLocation> stores = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            stores.add(StoreLocation.builder()
                    .storeId("ST-" + rand.nextInt(100_000, 999_000))
                    .name(brandProxy(rand) + " Store")
                    .location(randomUkGeoPoint())
                    .storeInStock(ThreadLocalRandom.current().nextBoolean())
                    .build());
        }
        return stores;
    }

    private String brandProxy(ThreadLocalRandom rand) {
        List<String> brands = data.getBrands();
        return brands.isEmpty() ? "Generic" : brands.get(rand.nextInt(brands.size()));
    }

    private GeoPoint randomUkGeoPoint() {
        double lat = 49.9 + ThreadLocalRandom.current().nextDouble(10.9);  // 49.9–60.8°N
        double lon = -8.6 + ThreadLocalRandom.current().nextDouble(10.4); // -8.6–1.8°E
        return new GeoPoint(lat, lon);
    }

    private List<CollectionPoint> generateCollectionPoints() {
        ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();
        int count = threadLocalRandom.nextInt(0, 4); // 0–3 collection points
        List<CollectionPoint> points = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String streetNum = String.valueOf(threadLocalRandom.nextInt(1, 150));
            points.add(CollectionPoint.builder()
                    .name(PRE_GENERATED_WORDS.get(threadLocalRandom.nextInt(PRE_GENERATED_WORDS.size())) + " Pickup Point")
                    .location(randomUkGeoPoint())
                    .address(streetNum + " " + PRE_GENERATED_STREETS.get(threadLocalRandom.nextInt(PRE_GENERATED_STREETS.size())))
                    .openingHours(generateOpeningHours())
                    .build());
        }
        return points;
    }

    private Map<String, Object> generateOpeningHours() {
        // Typical UK retail hours: Mon-Fri 9-18, Sat 9-17, Sun 10-16 (or closed)
        Map<String, Object> hours = new HashMap<>();
        hours.put("monday", "9:00-18:00");
        hours.put("tuesday", "9:00-18:00");
        hours.put("wednesday", "9:00-18:00");
        hours.put("thursday", "9:00-18:00");
        hours.put("friday", "9:00-18:00");
        hours.put("saturday", "9:00-17:00");
        // Sunday: 30% chance closed, otherwise 10:00-16:00
        if (ThreadLocalRandom.current().nextDouble() < 0.3) {
            hours.put("sunday", "closed");
        } else {
            hours.put("sunday", "10:00-16:00");
        }
        return hours;
    }

    // Helper to generate a semi‑realistic UK postcode (outward + inward)
    private String generateUkPostcode() {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        // 1. Fast random area lookup
        String area = UK_AREAS.get(random.nextInt(UK_AREAS.size()));

        // 2. Fast district generation (avoids String.valueOf overhead)
        int districtNum = random.nextInt(1, 100);

        // 3. Ultra-fast inward code generation (replaces String.format)
        int inwardNum = random.nextInt(1, 99);
        String inwardStr = (inwardNum < 10) ? "0" + inwardNum : String.valueOf(inwardNum);

        // Cast directly to char to avoid any ASCII string conversion overhead
        char inwardChar = (char) random.nextInt(65, 91);

        // 4. Combine efficiently using StringBuilder
        return new StringBuilder(12) // Pre-allocate capacity to avoid resizing
                .append(area)
                .append(districtNum)
                .append(' ')
                .append(inwardStr)
                .append(inwardChar)
                .toString();
    }

    /*static void main() {
        ThreadLocalRandom random = ThreadLocalRandom.current();

        // 1. Fast random area lookup
        String area = UK_AREAS.get(random.nextInt(UK_AREAS.size()));

        // 2. Fast district generation (avoids String.valueOf overhead)
        int districtNum = random.nextInt(1, 100);

        // 3. Ultra-fast inward code generation (replaces String.format)
        int inwardNum = random.nextInt(1, 99);
        String inwardStr = (inwardNum < 10) ? "0" + inwardNum : String.valueOf(inwardNum);

        // Cast directly to char to avoid any ASCII string conversion overhead
        char inwardChar = (char) random.nextInt(65, 91);

        // 4. Combine efficiently using StringBuilder
        String string = new StringBuilder(12) // Pre-allocate capacity to avoid resizing
                .append(area)
                .append(districtNum)
                .append(' ')
                .append(inwardStr)
                .append(inwardChar)
                .toString();
        System.out.println( string);

    }*/
}

