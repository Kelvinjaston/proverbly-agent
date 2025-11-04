package com.example.proverb.config;

import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.resolver.DefaultAddressResolverGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Random;

@Configuration
public class WebClientConfig {

    private static final Logger logger = LoggerFactory.getLogger(WebClientConfig.class);

    private static final String ZENQUOTES_API_URL = "https://zenquotes.io/api";
    private static final String QUOTABLE_API_URL = "https://api.quotable.io";

    private static final List<String> LOCAL_QUOTES = List.of(
            " Rise early, for the dawn rewards the diligent.",
            " Patience is bitter, but its fruit is sweet.",
            " Even a small stream can carve through rock with persistence.",
            " When you learn, teach; when you get, give.",
            " Every morning is a fresh page — write wisdom upon it."
    );
    private static final Random RANDOM = new Random();

    @Bean(name = "zenQuotesClient")
    public WebClient zenQuotesClient(WebClient.Builder builder) {
        try {
            WebClient client = builder.baseUrl(ZENQUOTES_API_URL).build();

            if (testApiConnection(client, "ZenQuotes", 3)) {
                logger.info(" Using ZenQuotes API as primary ({})", ZENQUOTES_API_URL);
                return client;
            } else {
                logger.warn(" ZenQuotes API unavailable — switching to Quotable fallback.");
            }
        } catch (Exception e) {
            logger.error(" Error initializing ZenQuotes WebClient: {}", e.getMessage());
        }

        return quotableClient(builder);
    }

    @Bean(name = "quotableClient")
    public WebClient quotableClient(WebClient.Builder builder) {
        try {
            HttpClient httpClient = HttpClient.create()
                    .resolver(DefaultAddressResolverGroup.INSTANCE)
                    .secure(ssl -> {
                        try {
                            ssl.sslContext(
                                    SslContextBuilder.forClient()
                                            .trustManager(InsecureTrustManagerFactory.INSTANCE)
                                            .build()
                            );
                        } catch (javax.net.ssl.SSLException e) {
                            throw new RuntimeException("Failed to configure SSL trust bypass", e);
                        }
                    })
                    .responseTimeout(Duration.ofSeconds(5));

            WebClient client = builder
                    .clientConnector(new ReactorClientHttpConnector(httpClient))
                    .baseUrl(QUOTABLE_API_URL)
                    .build();

            if (testApiConnection(client, "Quotable", 2)) {
                logger.info(" Using Quotable API as fallback ({})", QUOTABLE_API_URL);
                return client;
            } else {
                logger.warn(" Quotable API also unreachable — using local quotes instead.");
            }
        } catch (Exception e) {
            logger.error(" Error initializing Quotable WebClient: {}", e.getMessage());
        }

        return offlineWebClient(builder);
    }
    @Bean(name = "offlineWebClient")
    public WebClient offlineWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl("offline://quotes")
                .exchangeFunction(request -> {
                    String randomQuote = LOCAL_QUOTES.get(RANDOM.nextInt(LOCAL_QUOTES.size()));
                    logger.info(" Using local fallback quote: {}", randomQuote);

                    String json = String.format(
                            "{\"quote\": \"%s\", \"author\": \"Local Wisdom\"}",
                            randomQuote.replace("\"", "'")
                    );

                    DataBuffer buffer = new DefaultDataBufferFactory()
                            .wrap(json.getBytes(StandardCharsets.UTF_8));

                    return Mono.just(
                            ClientResponse.create(HttpStatus.OK)
                                    .header("Content-Type", "application/json")
                                    .body(Flux.just(buffer))
                                    .build()
                    );
                })
                .build();
    }
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
    private boolean testApiConnection(WebClient client, String apiName, int maxRetries) {
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                client.get()
                        .uri("/random")
                        .retrieve()
                        .bodyToMono(String.class)
                        .timeout(Duration.ofSeconds(3))
                        .block();
                logger.info("{} API reachable (attempt {}/{})", apiName, attempt, maxRetries);
                return true;
            } catch (Exception e) {
                logger.warn(" {} API connection failed (attempt {}/{}): {}", apiName, attempt, maxRetries, e.getMessage());
                try {
                    Thread.sleep(1000L * attempt);
                } catch (InterruptedException ignored) {}
            }
        }
        logger.error(" {} API not reachable after {} attempts.", apiName, maxRetries);
        return false;
    }
}
