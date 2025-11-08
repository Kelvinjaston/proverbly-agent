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

        try {
            boolean sendQuote = Math.random() < 0.5;

            Map<String, Object> response = new HashMap<>();

            if (sendQuote) {
                ExternalQuote quote = externalQuoteService.fetchRandomExternalQuote();

                if (quote != null && quote.getContent() != null) {
                    String message = " Inspirational Quote:\n\n" + quote.getContent();
                    if (quote.getAuthor() != null && !quote.getAuthor().isEmpty()) {
                        message += "\n\n— " + quote.getAuthor();
                    }

                    response.put("success", true);
                    response.put("message", message);
                    response.put("text", message);
                    response.put("type", "quote");
                    response.put("content", quote.getContent());
                    response.put("author", quote.getAuthor() != null ? quote.getAuthor() : "Unknown");

                    logger.info(" Sent quote: {}", quote.getContent());
                } else {
                    throw new Exception("Quote fetch returned null");
                }
            } else {
                Proverb proverb = proverbService.getRandomProverb();

                if (proverb != null) {
                    String message = String.format(" Nigerian Proverb (%s):\n\n%s\n\nMeaning:\n%s",
                            proverb.getLanguage(), proverb.getProverb(), proverb.getMeaning());

                    response.put("success", true);
                    response.put("message", message);
                    response.put("text", message);
                    response.put("type", "proverb");
                    response.put("content", proverb.getProverb());
                    response.put("meaning", proverb.getMeaning());
                    response.put("language", proverb.getLanguage());

                    logger.info("Sent proverb: {}", proverb.getProverb());
                } else {
                    throw new Exception("Proverb fetch returned null");
                }
            }
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching inspiration: {}", e.getMessage(), e);

            Map<String, Object> errorResponse = new HashMap<>();
            String fallbackMessage = "Keep pushing forward! Every day is a new opportunity. ";
            errorResponse.put("success", true);
            errorResponse.put("message", fallbackMessage);
            errorResponse.put("text", fallbackMessage);
            errorResponse.put("type", "fallback");

            return ResponseEntity.ok(errorResponse);
        }
    }

    @PostMapping("/quote")
    public ResponseEntity<Map<String, Object>> getQuoteForTelex(@RequestBody(required = false) Map<String, Object> request) {
        logger.info(" Telex Quote API request received: {}", request);

        try {
            ExternalQuote quote = externalQuoteService.fetchRandomExternalQuote();

            Map<String, Object> response = new HashMap<>();

            if (quote != null && quote.getContent() != null) {
                String message = quote.getContent();
                if (quote.getAuthor() != null && !quote.getAuthor().isEmpty()) {
                    message += " - " + quote.getAuthor();
                }
                response.put("success", true);
                response.put("message", message);
                response.put("text", message);
                response.put("quote", quote.getContent());
                response.put("author", quote.getAuthor() != null ? quote.getAuthor() : "Unknown");

                logger.info(" Quote sent successfully: {}", quote.getContent());
            } else {
                String fallbackMessage = "Keep pushing forward! Every day is a new opportunity. ";
                response.put("success", true);
                response.put("message", fallbackMessage);
                response.put("text", fallbackMessage);
                response.put("quote", "Keep pushing forward! Every day is a new opportunity.");
                response.put("author", "Proverbly Agent");

                logger.warn(" Using fallback quote");
            }
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error(" Error fetching quote: {}", e.getMessage(), e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Sorry, I couldn't fetch a quote right now. Please try again later. ");
            errorResponse.put("text", "Sorry, I couldn't fetch a quote right now. Please try again later. ");
            errorResponse.put("error", e.getMessage());

            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PostMapping("/proverb")
    public ResponseEntity<Map<String, Object>> getProverbForTelex(@RequestBody(required = false) Map<String, Object> request) {
        logger.info(" Telex Proverb API request received: {}", request);

        try {
            Proverb proverb = proverbService.getRandomProverb();

            Map<String, Object> response = new HashMap<>();

            if (proverb != null) {
                String message = String.format(" %s Proverb:\n%s\n\nMeaning: %s",
                        proverb.getLanguage(), proverb.getProverb(), proverb.getMeaning());

                response.put("success", true);
                response.put("message", message);
                response.put("text", message);
                response.put("proverb", proverb.getProverb());
                response.put("meaning", proverb.getMeaning());
                response.put("language", proverb.getLanguage());

                logger.info(" Proverb sent successfully: {}", proverb.getProverb());
            } else {
                String fallbackMessage = "Wisdom comes to those who seek it. Try again! ";
                response.put("success", true);
                response.put("message", fallbackMessage);
                response.put("text", fallbackMessage);

                logger.warn(" Using fallback proverb");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error(" Error fetching proverb: {}", e.getMessage(), e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Sorry, I couldn't fetch a proverb right now. Please try again later. ");
            errorResponse.put("text", "Sorry, I couldn't fetch a proverb right now. Please try again later. ");
            errorResponse.put("error", e.getMessage());

            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "healthy");
        response.put("service", "Proverbly Agent - Telex Integration");
        response.put("message", "Ready to inspire! ");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/webhook")
    public Map<String,Object> handleTelexMessage(@RequestBody Map<String,Object> payload){
        logger.info(" Telex webhook message received: {}", payload);

        String message = extractMessageText(payload);

        if (message == null || message.isBlank()) {
            return Map.of("text", "");
        }
        Proverb proverb = null;
        String replyText;
        String detectedLanguage = null;

        if (message.startsWith("/start") || message.startsWith("/help")) {
            replyText = "Welcome to the Proverbly Agent! \n\n" +
                    "Commands:\n" +
                    "• /proverb - Get a random Nigerian proverb\n" +
                    "• /quote - Get an inspirational quote\n" +
                    "• /inspire - Get random inspiration (quote or proverb)\n" +
                    "• Specify language: 'Igbo proverb', 'Yoruba proverb', etc.";
        } else if (message.startsWith("/proverb") || message.contains("proverb")) {
            detectedLanguage = NIGERIAN_LANGUAGES.stream()
                    .filter(message::contains)
                    .findFirst()
                    .orElse(null);

            if (detectedLanguage != null) {
                proverb = proverbService.getRandomByLanguage(detectedLanguage);
            } else {
                proverb = proverbService.getRandomProverb();
            }
            if (proverb != null) {
                replyText = String.format(" Proverb (%s):\n%s\n\nMeaning:\n%s",
                        proverb.getLanguage(), proverb.getProverb(), proverb.getMeaning());
            } else {
                replyText = "Sorry, I could not find a proverb. Try asking again with a language name like 'Igbo' or 'Hausa'.";
            }
        } else if (message.startsWith("/quote") || message.contains("quote")) {
            try {
                ExternalQuote quote = externalQuoteService.fetchRandomExternalQuote();
                if (quote != null && quote.getContent() != null) {
                    replyText = "✨ " + quote.getContent();
                    if (quote.getAuthor() != null && !quote.getAuthor().isEmpty()) {
                        replyText += " - " + quote.getAuthor();
                    }
                } else {
                    replyText = "Keep pushing forward! Every day is a new opportunity.";
                }
            } catch (Exception e) {
                logger.error("Error fetching quote: {}", e.getMessage());
                replyText = "Sorry, I couldn't fetch a quote right now. Try again!";
            }
        } else if (message.startsWith("/inspire") || message.contains("inspiration") || message.contains("motivate")) {
            try {
                if (Math.random() < 0.5) {
                    ExternalQuote quote = externalQuoteService.fetchRandomExternalQuote();
                    if (quote != null && quote.getContent() != null) {
                        replyText = " " + quote.getContent();
                        if (quote.getAuthor() != null && !quote.getAuthor().isEmpty()) {
                            replyText += " - " + quote.getAuthor();
                        }
                    } else {
                        replyText = "Keep pushing forward! Every day is a new opportunity.";
                    }
                } else {
                    proverb = proverbService.getRandomProverb();
                    if (proverb != null) {
                        replyText = String.format(" Proverb (%s):\n%s\n\nMeaning:\n%s",
                                proverb.getLanguage(), proverb.getProverb(), proverb.getMeaning());
                    } else {
                        replyText = "Wisdom comes to those who seek it. Try again!";
                    }
                }
            } catch (Exception e) {
                logger.error("Error fetching inspiration: {}", e.getMessage());
                replyText = "Sorry, I couldn't fetch inspiration right now. Try again!";
            }
        } else {
            replyText = "I'm here to inspire you! Try:\n• /proverb - Get Nigerian wisdom\n• /quote - Get inspiration\n• /inspire - Surprise me!\n• /help - See all commands";
        }
        Map<String,Object> response = new HashMap<>();
        response.put("text", replyText);
        response.put("response_type", "in_channel");

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