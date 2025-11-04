package com.example.proverb;

import com.example.proverb.model.Proverb;
import com.example.proverb.repo.ProverbRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import java.io.InputStream;
import java.util.List;

@SpringBootApplication
@EnableScheduling
public class ProverblyAgentApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProverblyAgentApplication.class, args);
	}

	@Bean
	CommandLineRunner seed(ProverbRepository repo, ResourceLoader resourceLoader) {
		return args -> {
			if (repo.count() == 0) {
				ObjectMapper mapper = new ObjectMapper();

				Resource resource = resourceLoader.getResource("classpath:proverbs.json");

				try (InputStream inputStream = resource.getInputStream()) {
					List<Proverb> proverbs = mapper.readValue(
							inputStream,
							new TypeReference<List<Proverb>>() {}
					);

					repo.saveAll(proverbs);
					System.out.println(" Database Seeded Successfully with " + proverbs.size() + " proverbs.");

				} catch (Exception e) {
					System.err.println(" Failed to load proverbs from JSON file: " + e.getMessage());
				}
			} else {
				System.out.println(" Database already contains data. Skipping seeding.");
			}
		};
	}
}