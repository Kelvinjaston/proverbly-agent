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

                if (quote != null && quote.getContent() != null) {
                    String message = "✨ Inspirational Quote:\n\n" + quote.getContent();
                    if (quote.getAuthor() != null && !quote.getAuthor().isEmpty()) {
                        message += "\n\n— " + quote.getAuthor();
                    }

                    response.put("success", true);
                    response.put("response", message);
                    response.put("type", "quote");
                    response.put("content_type", "text");
                    response.put("author", quote.getAuthor() != null ? quote.getAuthor() : "Unknown");
                } else {
                    throw new Exception("Quote fetch returned null");
                }

            } else {
                Proverb proverb = proverbService.getRandomProverb();
                if (proverb != null) {
                    String message = String.format("🪶 Nigerian Proverb (%s):\n\n%s\n\nMeaning:\n%s",
                            proverb.getLanguage(), proverb.getProverb(), proverb.getMeaning());

                    response.put("success", true);
                    response.put("response", message);
                    response.put("type", "proverb");
                    response.put("content_type", "text");
                } else {
                    throw new Exception("Proverb fetch returned null");
                }
            }
        } catch (Exception e) {
            logger.error("Error fetching inspiration: {}", e.getMessage(), e);
            String fallbackMessage = "🌅 Keep pushing forward! Every day is a new opportunity.";

            response.put("success", true);
            response.put("response", fallbackMessage);
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

    @PostMapping("/webhook")
    public Map<String, Object> handleTelexMessage(@RequestBody Map<String, Object> payload) {
        logger.info("Telex webhook message received: {}", payload);

        String message = extractMessageText(payload);
        Map<String, Object> response = new HashMap<>();
        String replyText;

        if (message == null || message.isBlank()) {
            replyText = "👋 Welcome to *Proverbly Agent!* I'm ready to inspire you. Try `/inspire` or type a command like 'proverb'.";

            response.put("success", true);
            response.put("response", replyText);
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

                proverb = (detectedLanguage != null)
                        ? proverbService.getRandomByLanguage(detectedLanguage)
                        : proverbService.getRandomProverb();

                if (proverb != null) {
                    replyText = String.format("🪶 %s Proverb:\n%s\n\nMeaning:\n%s",
                            proverb.getLanguage(), proverb.getProverb(), proverb.getMeaning());
                } else {
                    replyText = "I couldn't find a proverb right now. Try again or specify a language.";
                }

            } else if (message.contains("quote")) {
                ExternalQuote quote = externalQuoteService.fetchRandomExternalQuote();
                if (quote != null && quote.getContent() != null) {
                    replyText = "💡 " + quote.getContent();
                    if (quote.getAuthor() != null && !quote.getAuthor().isEmpty()) {
                        replyText += "\n\n— " + quote.getAuthor();
                    }
                } else {
                    replyText = "Keep believing in yourself — brighter days are ahead!";
                }

            } else {
                replyText = "🌻 I'm here to inspire you! Try:\n" +
                        "• `/quote` - Inspirational quote\n" +
                        "• `/proverb` - Nigerian wisdom\n" +
                        "• `/inspire` - A random pick!";
            }

        } catch (Exception e) {
            logger.error("Error in Telex message handler: {}", e.getMessage(), e);
            replyText = "⚠️ Something went wrong fetching inspiration. Try again later.";
        }

        response.put("success", true);
        response.put("response", replyText);

        return response;
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