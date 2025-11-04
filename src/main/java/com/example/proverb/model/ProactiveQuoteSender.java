package com.example.proverb.model;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ProactiveQuoteSender {

    private static final Logger logger = LoggerFactory.getLogger(ProactiveQuoteSender.class);

    private final WebClient zenQuotesClient;
    private final WebClient quotableClient;
    private final WebClient localApiClient = WebClient.create("http://localhost:8080");

    private String currentApiSource;

    @Scheduled(fixedRate = 3600000)
    public void fetchAndSendProverb() {
        try {
            currentApiSource = "ZenQuotes";
            String endpoint = "/random";

            logger.info(" Fetching quote from [{}] at {}", currentApiSource, now());

            zenQuotesClient.get()
                    .uri(endpoint)
                    .retrieve()
                    .bodyToMono(String.class)
                    .flatMap(this::handleQuoteResponse)
                    .onErrorResume(e -> {
                        logger.warn(" ZenQuotes failed — switching to Quotable: {}", e.getMessage());
                        return fetchFromQuotable();
                    })
                    .block();

        } catch (Exception e) {
            logger.error(" Unexpected error during proactive fetch: {}", e.getMessage());
        }
    }
    private Mono<Void> fetchFromQuotable() {
        currentApiSource = "Quotable";
        logger.info(" Attempting fallback to [{}] at {}", currentApiSource, now());

        return quotableClient.get()
                .uri("/random")
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(this::handleQuoteResponse)
                .onErrorResume(e -> {
                    logger.error(" Both APIs failed: {}", e.getMessage());
                    return Mono.empty();
                });
    }
    private Mono<Void> handleQuoteResponse(String response) {
        if (response == null || response.isEmpty()) {
            logger.warn(" Empty response from [{}]", currentApiSource);
            return Mono.empty();
        }

        String trimmed = response.length() > 120 ? response.substring(0, 120) + "..." : response;
        logger.info(" [{}] Quote Response: {}", currentApiSource, trimmed);

        String quoteText = extractQuote(response);

        if (quoteText == null || quoteText.isBlank()) {
            logger.warn("No valid quote text found in [{}] response", currentApiSource);
            return Mono.empty();
        }

        return sendToBackend(quoteText);
    }
    private String extractQuote(String json) {
        try {
            if (json.contains("\"q\"")) {
                int start = json.indexOf("\"q\"") + 5;
                int end = json.indexOf("\"", start);
                return json.substring(start, end);
            }
            else if (json.contains("\"content\"")) {
                int start = json.indexOf("\"content\"") + 11;
                int end = json.indexOf("\"", start);
                return json.substring(start, end);
            }
        } catch (Exception ignored) {}
        return null;
    }
    private Mono<Void> sendToBackend(String quoteText) {
        logger.info("Sending fetched quote to backend: {}", quoteText);

        return localApiClient.post()
                .uri("/api/proverbs/random")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "quote", quoteText,
                        "source", currentApiSource,
                        "timestamp", now()
                ))
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(res -> logger.info("Backend response: {}", res))
                .then();
    }
    private String now() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

}
