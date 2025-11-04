package com.example.proverb.controller;

import com.example.proverb.dto.ExternalQuote;
import com.example.proverb.service.ExternalQuoteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        logger.info(" Fetching random quote from external API...");

        ExternalQuote quote = externalQuoteService.fetchRandomExternalQuote();

        if (quote == null || quote.getContent() == null) {
            logger.warn("Failed to fetch quote — returning fallback message");

            ExternalQuote fallback = new ExternalQuote();
            fallback.setContent("No quote available at the moment.");
            fallback.setAuthor("System");
            return fallback;
        }

        logger.info(" Successfully fetched quote: {}", quote.getContent());
        return quote;
    }
}
