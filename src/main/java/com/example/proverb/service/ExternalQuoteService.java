package com.example.proverb.service;

import com.example.proverb.dto.ExternalQuote;
import com.example.proverb.model.Proverb;
import com.example.proverb.repo.ProverbRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class ExternalQuoteService {

    private static final Logger logger = LoggerFactory.getLogger(ExternalQuoteService.class);
    private static final String ZEN_QUOTES_ENDPOINT = "random";

    private final WebClient zenQuotesClient;
    private final WebClient offlineWebClient;
    private final ProverbRepository proverbRepository;

    public ExternalQuoteService(
            @Qualifier("zenQuotesClient") WebClient zenQuotesClient,
            @Qualifier("offlineWebClient") WebClient offlineWebClient,
            ProverbRepository proverbRepository) {

        this.zenQuotesClient = zenQuotesClient;
        this.offlineWebClient = offlineWebClient;
        this.proverbRepository = proverbRepository;
    }
    public ExternalQuote fetchRandomExternalQuote() {
        try {
            Map<?, ?>[] rawResponse = zenQuotesClient.get()
                    .uri(ZEN_QUOTES_ENDPOINT)
                    .retrieve()
                    .bodyToMono(Map[].class)
                    .block();

            if (rawResponse != null && rawResponse.length > 0) {
                Map<?, ?> q = rawResponse[0];
                ExternalQuote quote = new ExternalQuote();
                quote.setContent((String) q.get("q"));
                quote.setAuthor((String) q.get("a"));

                logger.info("[ZenQuotes] \"{}\" — {}", quote.getContent(), quote.getAuthor());
                saveQuoteAsProverb(quote, "ZenQuotes");
                return quote;
            } else {
                throw new RuntimeException("Empty response from ZenQuotes");
            }

        } catch (Exception ex) {
            logger.warn(" ZenQuotes API failed: {} — trying offline source...", ex.getMessage());

            try {
                String jsonResponse = offlineWebClient.get()
                        .uri("/random")
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                ExternalQuote local = new ExternalQuote();
                local.setContent(jsonResponse.replace("{\"quote\":", "")
                        .replace("}", "")
                        .replace("\"", "")
                        .trim());
                local.setAuthor("Local Wisdom");

                saveQuoteAsProverb(local, "Offline");
                return local;

            } catch (Exception fallbackEx) {
                logger.error(" Offline fallback failed: {}", fallbackEx.getMessage());
                ExternalQuote backup = new ExternalQuote();
                backup.setContent("Even when APIs fail, persistence wins the day.");
                backup.setAuthor("AI Agent");
                saveQuoteAsProverb(backup, "Fallback");
                return backup;
            }
        }
    }
    private void saveQuoteAsProverb(ExternalQuote quote, String source) {
        try {
            Proverb proverb = new Proverb();
            proverb.setText(quote.getContent());
            proverb.setAuthor(quote.getAuthor());
            proverb.setCategory(source + " Quote");
            proverb.setCreatedAt(LocalDateTime.now());

            proverbRepository.save(proverb);
            logger.info(" Saved new proverb from {} — \"{}\"", source, quote.getContent());
        } catch (Exception e) {
            logger.error(" Failed to save proverb from {}: {}", source, e.getMessage());
        }
    }
}
