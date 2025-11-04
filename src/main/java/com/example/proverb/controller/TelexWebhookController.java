package com.example.proverb.controller;

import com.example.proverb.model.Proverb;
import com.example.proverb.service.ProverbService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/telex")
@RequiredArgsConstructor
public class TelexWebhookController {

    private final ProverbService proverbService;
    private static final List<String> NIGERIAN_LANGUAGES = List.of("yoruba", "igbo", "hausa", "efik", "ibibio");

    /**
     * Utility method to safely extract the user's message text from the Telegram/telex payload.
     * The expected structure is: payload -> "message" -> "text"
     */
    private String extractMessageText(Map<String, Object> payload) {
        try {
            // Check for the "message" key first
            if (payload.containsKey("message")) {
                Object messageObj = payload.get("message");
                if (messageObj instanceof Map) {
                    Map<?, ?> message = (Map<?, ?>) messageObj;

                    // Now check for the "text" key inside the message map
                    if (message.containsKey("text")) {
                        return message.get("text").toString().toLowerCase();
                    }
                }
            }
        } catch (Exception e) {
            // Log the error if payload structure is unexpected
            System.err.println("Error parsing message payload: " + e.getMessage());
        }
        return null;
    }

    @PostMapping
    public Map<String,Object> handleTelexMessage(@RequestBody Map<String,Object> payload){
        // Use the robust extraction method
        String message = extractMessageText(payload);

        if (message == null || message.isBlank()) {
            return Map.of("text", ""); // Ignore non-text messages or unparsed payloads
        }

        // --- Core Logic ---

        Proverb proverb = null;
        String replyText;
        String detectedLanguage = null;

        if (message.startsWith("/start") || message.startsWith("/help")) {
            replyText = "Welcome to the Proverbly Agent! Send the command '/proverb' to get a random one. You can also specify a Nigerian language (e.g., 'Igbo proverb').";
        } else if (message.startsWith("/proverb")) {
            // Check if any language is specified after /proverb
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
                // Combine proverb and meaning into a nice reply string
                replyText = String.format("✨ Proverb (%s):\n%s\n\nMeaning:\n%s",
                        proverb.getLanguage(), proverb.getProverb(), proverb.getMeaning());
            } else {
                replyText = "Sorry, I could not find a proverb. Try asking again with a language name like 'Igbo' or 'Hausa'.";
            }
        } else {
            replyText = "I'm a simple agent. Try sending me '/proverb' or '/help'.";
        }

        // --- Build Telex Response Map ---
        // Returning a Map containing the "text" is the standard way to reply via webhook.
        Map<String,Object> response = new HashMap<>();
        response.put("text", replyText);

        // This is often required for telex/slack style integration replies
        response.put("response_type", "in_channel");

        return response;
    }
}
