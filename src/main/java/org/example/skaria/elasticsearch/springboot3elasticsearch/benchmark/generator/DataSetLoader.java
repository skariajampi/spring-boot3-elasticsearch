package org.example.skaria.elasticsearch.springboot3elasticsearch.benchmark.generator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import lombok.Getter;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

@Profile("benchmark")
@Component
@Getter
public class DataSetLoader {

    private final List<String> brands;
    private final List<String> adjectives;
    private final Map<String, List<String>> categories;
    private final ResourceLoader resourceLoader;

    public DataSetLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
        this.brands = loadText("/benchmark/brands.txt");
        this.adjectives = loadText("/benchmark/adjectives.txt");
        this.categories = loadYaml("/benchmark/categories.yml");
    }

    private List<String> loadText(String path) {

        try (InputStream stream = resource(path);
             BufferedReader reader =
                     new BufferedReader(new InputStreamReader(stream))) {

            return reader.lines()
                    .map(String::trim)
                    .filter(line -> !line.isBlank())
                    .toList();

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Cannot load dataset: " + path, e);
        }
    }

    private Map<String, List<String>> loadYaml(String path) {

        try (InputStream stream = resource(path)) {

            YAMLMapper mapper = new YAMLMapper();

            return mapper.readValue(
                    stream,
                    new TypeReference<Map<String, List<String>>>() {}
            );

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Cannot load YAML dataset: " + path, e);
        }
    }

    private InputStream resource(String path) {

        try {
            return resourceLoader.getResource("classpath:" + path).getInputStream();
        } catch (IOException e) {
            throw new IllegalStateException("Unable to load " + path, e);
        }
    }
}
