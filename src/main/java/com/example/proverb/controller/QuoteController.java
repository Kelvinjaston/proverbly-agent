package com.example.proverb.controller;

import com.example.proverb.dto.ExternalQuote;
import com.example.proverb.service.ExternalQuoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/quote")
public class QuoteController {

    private static final Logger logger = LoggerFactory.getLogger(QuoteController.class);
    private final ExternalQuoteService externalQuoteService;

    public QuoteController(ExternalQuoteService externalQuoteService) {
        this.externalQuoteService = externalQuoteService;
    }
    @GetMapping("/random-external")
    public ExternalQuote getRandomExternalQuote() {
        logger.info("GET request: Fetching random quote from external API...");

        ExternalQuote quote = externalQuoteService.fetchRandomExternalQuote();

        if (quote == null || quote.getContent() == null) {
            logger.warn("GET: Failed to fetch quote — returning fallback message");
            ExternalQuote fallback = new ExternalQuote();
            fallback.setContent("No quote available at the moment.");
            fallback.setAuthor("System");
            return fallback;
        }

        logger.info("GET: Successfully fetched quote.");
        return quote;
    }
    @PostMapping("/random-external")
    public ResponseEntity<Map<String, Object>> getRandomExternalQuotePost() {
        logger.info("POST request: Fetching random quote for telex.im agent...");

        ExternalQuote quote = externalQuoteService.fetchRandomExternalQuote();

        if (quote == null || quote.getContent() == null) {
            logger.warn("POST: Failed to fetch quote — using structured fallback");
            quote = new ExternalQuote();
            quote.setContent("No quote available at the moment. Try again later.");
            quote.setAuthor("System");
        }
        Map<String, Object> response = new HashMap<>();
        response.put("title", "🌟 Daily Inspiration 🌟");
        response.put("description", quote.getContent());
        response.put("author", quote.getAuthor());
        response.put("source", "Proverbly Agent");

        logger.info("POST: Successfully fetched and structured quote.");
        return ResponseEntity.ok(response);
    }
}