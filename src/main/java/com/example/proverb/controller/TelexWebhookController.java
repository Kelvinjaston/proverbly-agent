package com.example.proverb.controller;

import com.example.proverb.model.Proverb;
import com.example.proverb.service.ProverbService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/telex")
@RequiredArgsConstructor
public class TelexWebhookController {
    private final ProverbService proverbService;
    private static final List<String> NIGERIAN_LANGUAGES = List.of("yoruba", "igbo", "hausa", "efik", "ibibio");

    @PostMapping
    public Map<String,Object> handleTelexMessage(@RequestBody Map<String,Object> payload){
        String message = payload.getOrDefault("message", "").toString().toLowerCase();

        Proverb proverb;

        String detectedLanguage = NIGERIAN_LANGUAGES.stream()
                .filter(message::contains)
                .findFirst()
                .orElse(null);

        if (detectedLanguage != null) {
            proverb = proverbService.getRandomByLanguage(detectedLanguage);
        } else {
            proverb = proverbService.getRandomProverb();
        }
        Map<String,Object> response = new HashMap<>();
        response.put("response_type", "in_channel");

        if (proverb != null) {
            response.put("text", proverb.getProverb() + " (" + proverb.getLanguage() + ")");
            response.put("meaning", proverb.getMeaning());
        } else {
            response.put("text", "Sorry, I could not find a proverb for your request.");
            response.put("meaning", "Try asking again with a language name like 'Igbo' or 'Hausa'.");
        }
        return response;
    }
}