package com.example.proverb.controller;

import com.example.proverb.dto.ExternalQuote;
import com.example.proverb.model.Proverb;
import com.example.proverb.service.ExternalQuoteService;
import com.example.proverb.service.ProverbService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/telex")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TelexWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(TelexWebhookController.class);

    private final ProverbService proverbService;
    private final ExternalQuoteService externalQuoteService;

    private static final List<String> NIGERIAN_LANGUAGES = List.of("yoruba", "igbo", "hausa", "efik", "ibibio");

    @PostMapping("/inspire")
    public ResponseEntity<Map<String, Object>> getRandomInspiration(@RequestBody(required = false) Map<String, Object> request) {
        logger.info("Telex Random Inspiration request received: {}", request);

        Map<String, Object> response = new HashMap<>();
        try {
            boolean sendQuote = Math.random() < 0.5;

            if (sendQuote) {
                ExternalQuote quote = externalQuoteService.fetchRandomExternalQuote();

                logger.info("Quote fetch result: {}", quote);

                if (quote != null && quote.getContent() != null) {
                    String message = "✨ Inspirational Quote:\n\n" + quote.getContent();
                    if (quote.getAuthor() != null && !quote.getAuthor().isEmpty()) {
                        message += "\n\n— " + quote.getAuthor();
                    }

                    response.put("success", true);
                    response.put("text", message); // FIX: Use 'text'
                    response.put("type", "quote");
                    response.put("content_type", "text");
                    response.put("author", quote.getAuthor() != null ? quote.getAuthor() : "Unknown");
                } else {
                    logger.error("Quote service returned null or empty content");
                    throw new Exception("Quote fetch returned null");
                }

            } else {
                Proverb proverb = proverbService.getRandomProverb();

                logger.info("Proverb fetch result: {}", proverb);

                if (proverb != null) {
                    String message = String.format("🪶 Nigerian Proverb (%s):\n\n%s\n\nMeaning:\n%s",
                            proverb.getLanguage(), proverb.getProverb(), proverb.getMeaning());

                    response.put("success", true);
                    response.put("text", message); // FIX: Use 'text'
                    response.put("type", "proverb");
                    response.put("content_type", "text");
                } else {
                    logger.error("Proverb service returned null");
                    throw new Exception("Proverb fetch returned null");
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching inspiration: {}", e.getMessage(), e);
            String fallbackMessage = "🌅 Keep pushing forward! Every day is a new opportunity.";

            response.put("success", true);
            response.put("text", fallbackMessage); // FIX: Use 'text'
            response.put("type", "fallback");
            response.put("content_type", "text");
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("service", "Proverbly Agent - Telex Integration");
        response.put("message", "✅ Ready to inspire!");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/diagnostic")
    public Map<String, Object> diagnosticTestGet() {
        logger.info("Running comprehensive diagnostic test");

        Map<String, Object> response = new HashMap<>();
        Map<String, Object> diagnostic = new HashMap<>();

        try {
            // Test Proverb Service - General
            try {
                Proverb randomProverb = proverbService.getRandomProverb();
                diagnostic.put("proverbService", randomProverb != null ? "WORKING" : "FAILING - NO PROVERBS");
                diagnostic.put("proverbData", randomProverb);
            } catch (Exception e) {
                diagnostic.put("proverbService", "FAILING - " + e.getMessage());
            }

            // Test All Nigerian Languages
            Map<String, Object> languageTests = new HashMap<>();
            for (String language : NIGERIAN_LANGUAGES) {
                try {
                    Proverb langProverb = proverbService.getRandomByLanguage(language);
                    languageTests.put(language, langProverb != null ? "FOUND" : "NOT FOUND");
                } catch (Exception e) {
                    languageTests.put(language, "ERROR - " + e.getMessage());
                }
            }
            diagnostic.put("languageProverbs", languageTests);

            // Test Quote Service
            try {
                ExternalQuote randomQuote = externalQuoteService.fetchRandomExternalQuote();
                diagnostic.put("quoteService", randomQuote != null ? "WORKING" : "FAILING - NO QUOTES");
                diagnostic.put("quoteData", randomQuote);
            } catch (Exception e) {
                diagnostic.put("quoteService", "FAILING - " + e.getMessage());
            }

            response.put("success", true);
            response.put("diagnostic", diagnostic);
            response.put("message", "Comprehensive diagnostic completed");

        } catch (Exception e) {
            logger.error("Diagnostic test failed: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("error", e.getMessage());
        }

        return response;
    }

    @PostMapping("/diagnostic")
    public Map<String, Object> diagnosticTest(@RequestBody(required = false) Map<String, Object> payload) {
        return diagnosticTestGet();
    }

    @PostMapping("/seed-sample")
    public Map<String, Object> seedSampleData() {
        Map<String, Object> response = new HashMap<>();
        try {
            logger.info("Seeding sample proverbs...");

            response.put("success", true);
            response.put("message", "Sample data seeding endpoint ready - implement proverbService.seedSampleProverbs()");
        } catch (Exception e) {
            logger.error("Error seeding sample data: {}", e.getMessage());
            response.put("success", false);
            response.put("error", e.getMessage());
        }
        return response;
    }

    @GetMapping("/webhook")
    public Map<String, Object> handleTelexValidation() {
        // This handler ensures the URL responds to GET for validation checks.
        Map<String, Object> response = new HashMap<>();

        response.put("success", true);
        response.put("text", "Proverbly Agent is validated and ready to receive POST messages.");
        response.put("response_type", "in_channel");
        response.put("content_type", "text");

        return response;
    }

    @PostMapping("/webhook")
    public Map<String, Object> handleTelexMessage(@RequestBody Map<String, Object> payload) {
        logger.info("Telex webhook message received: {}", payload);

        String message = extractMessageText(payload);
        Map<String, Object> response = new HashMap<>();
        String replyText;

        if (message == null || message.isBlank()) {
            replyText = "👋 Welcome to *Proverbly Agent!* I'm ready to inspire you. Try `/inspire` or type a command like 'proverb'.";

            response.put("success", true);
            response.put("text", replyText); // FIX: Use 'text'
            response.put("response_type", "in_channel"); // ADDED
            response.put("content_type", "text"); // ADDED
            return response;
        }

        Proverb proverb = null;
        String detectedLanguage;

        try {
            if (message.startsWith("/start") || message.startsWith("/help")) {
                replyText = "👋 Welcome to *Proverbly Agent!* \n\nCommands:\n" +
                        "• `/proverb` - Get a random Nigerian proverb\n" +
                        "• `/quote` - Get an inspirational quote\n" +
                        "• `/inspire` - Surprise you with either\n" +
                        "• You can also say: *Yoruba proverb*, *Hausa proverb*, etc.";
            } else if (message.contains("proverb")) {
                detectedLanguage = NIGERIAN_LANGUAGES.stream()
                        .filter(message::contains)
                        .findFirst()
                        .orElse(null);

                try {
                    proverb = (detectedLanguage != null)
                            ? proverbService.getRandomByLanguage(detectedLanguage)
                            : proverbService.getRandomProverb();

                    logger.info("Proverb search - Language: {}, Found: {}", detectedLanguage, proverb);

                    if (proverb != null && proverb.getProverb() != null) {
                        replyText = String.format("🪶 %s Proverb:\n\n%s\n\nMeaning:\n%s",
                                proverb.getLanguage(), proverb.getProverb(), proverb.getMeaning());
                    } else {
                        replyText = getFallbackProverb(detectedLanguage);
                        logger.warn("Proverb found but content is null, using fallback");
                    }
                } catch (Exception e) {
                    logger.error("Error in proverb service: {}", e.getMessage());
                    replyText = getFallbackProverb(detectedLanguage);
                }

            } else if (message.contains("quote")) {
                ExternalQuote quote = externalQuoteService.fetchRandomExternalQuote();

                logger.info("Quote fetch - Found: {}", quote);

                if (quote != null && quote.getContent() != null) {
                    replyText = "✨ Inspirational Quote:\n\n" + quote.getContent();
                    if (quote.getAuthor() != null && !quote.getAuthor().isEmpty()) {
                        replyText += "\n\n— " + quote.getAuthor();
                    }
                } else {
                    replyText = getFallbackQuote();
                    logger.warn("Using fallback quote - External service unavailable");
                }

            } else {
                logger.info("No specific command detected, providing random inspiration");
                // Call /inspire endpoint and get the response body
                Map<String, Object> inspireResponse = getRandomInspiration(payload).getBody();

                // Extract the content from the inspire response
                if (inspireResponse != null && inspireResponse.containsKey("text")) {
                    replyText = (String) inspireResponse.get("text");
                } else {
                    replyText = "I couldn't find anything inspiring right now, but I'm trying!";
                }
            }

        } catch (Exception e) {
            logger.error("CRITICAL ERROR IN MESSAGE HANDLER: {}", e.getMessage(), e);
            replyText = "⚠️ Something went wrong fetching content, but remember: 'The only way to do great work is to love what you do.' – Steve Jobs";
        }

        response.put("success", true);
        response.put("text", replyText); // FIX: Use 'text'
        response.put("response_type", "in_channel"); // ADDED
        response.put("content_type", "text"); // ADDED

        return response;
    }

    private String getFallbackProverb(String language) {
        Map<String, String> fallbackProverbs = Map.of(
                "yoruba", "🪶 Yoruba Proverb:\n\nÌwà l'ẹ̀ṣọ́\n\nMeaning:\nCharacter is religion",
                "igbo", "🪶 Igbo Proverb:\n\nEgbe bere, ugo bere\n\nMeaning:\nLet the eagle perch, let the hawk perch",
                "hausa", "🪶 Hausa Proverb:\n\nRashin ruwa, ragon zaki\n\nMeaning:\nLack of water is death to the lion",
                "efik", "🪶 Efik Proverb:\n\nUdeme kiet ididaha nda\n\nMeaning:\nOne finger cannot lift a load",
                "ibibio", "𪎨 Ibibio Proverb:\n\nEkpo akpa enyin, ikpaha utọn̄\n\nMeaning:\nThe spirit is blind but not deaf",
                "general", "𪎨 Nigerian Proverb:\n\nHowever long the night, the day is sure to come\n\nMeaning:\nNo situation lasts forever"
        );

        String proverb = fallbackProverbs.get(language != null ? language : "general");
        return proverb != null ? proverb : fallbackProverbs.get("general");
    }

    private String getFallbackQuote() {
        return "✨ Inspirational Quote:\n\nThe only way to do great work is to love what you do.\n\n— Steve Jobs";
    }

    private String extractMessageText(Map<String, Object> payload) {
        try {
            if (payload.containsKey("message")) {
                Object messageObj = payload.get("message");
                if (messageObj instanceof Map) {
                    Map<?, ?> message = (Map<?, ?>) messageObj;
                    if (message.containsKey("text")) {
                        return message.get("text").toString().toLowerCase();
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error parsing message payload: {}", e.getMessage());
        }
        return null;
    }
}