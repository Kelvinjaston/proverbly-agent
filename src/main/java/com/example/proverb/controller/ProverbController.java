package com.example.proverb.controller;

import com.example.proverb.dto.ProverbResponse;
import com.example.proverb.model.Proverb;
import com.example.proverb.repo.ProverbRepository;
import com.example.proverb.service.ProverbService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/proverbs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProverbController {

    private final ProverbService proverbService;
    private final ProverbRepository proverbRepository;

    private ProverbResponse mapToResponse(Proverb proverb) {
        ProverbResponse dto = new ProverbResponse();
        dto.setId(proverb.getId());
        dto.setLanguage(proverb.getLanguage());
        dto.setProverb(proverb.getProverb());
        dto.setTranslation(proverb.getTranslation());
        dto.setMeaning(proverb.getMeaning());
        return dto;
    }
    @GetMapping("/random")
    public ProverbResponse getRandomProverb() {
        return mapToResponse(proverbService.getRandomProverb());
    }
    @GetMapping("/random/{language}")
    public ProverbResponse getRandomByLanguage(@PathVariable String language) {
        return mapToResponse(proverbService.getRandomByLanguage(language));
    }
    @GetMapping("/random/all")
    public ProverbResponse getRandomAcrossLanguages() {
        return mapToResponse(proverbService.getRandomProverb());
    }
    @GetMapping
    public List<ProverbResponse> getAllProverbs() {
        return proverbService.getAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
    @PostMapping
    public ProverbResponse addProverb(@RequestBody Proverb proverb) {
        Proverb saved = proverbService.add(proverb);
        return mapToResponse(saved);
    }
    @PostMapping("/random")
    public ResponseEntity<String> saveFetchedQuote(@RequestBody Map<String, Object> payload) {
        try {
            String quote = payload.getOrDefault("quote", "").toString();
            String source = payload.getOrDefault("source", "Unknown").toString();
            if (quote.isBlank()) {
                return ResponseEntity.badRequest().body("Quote text cannot be empty");
            }
            String meaningAttribution = "Attribution: Imported quote from " + source;

            Proverb proverb = Proverb.builder()
                    .language("English")
                    .proverb(quote)
                    .translation("Original quote (not a proverb translation)")
                    .meaning(meaningAttribution)
                    .build();
            proverbRepository.save(proverb);
            return ResponseEntity.ok("✅ Quote saved successfully from " + source);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Error saving quote: " + e.getMessage());
        }
    }
}
