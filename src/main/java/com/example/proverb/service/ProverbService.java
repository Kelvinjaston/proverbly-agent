package com.example.proverb.service;

import com.example.proverb.exception.ResourceNotFoundException;
import com.example.proverb.model.Proverb;
import com.example.proverb.repo.ProverbRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProverbService {

    private static final Logger logger = LoggerFactory.getLogger(ProverbService.class);

    private final ProverbRepository proverbRepository;
    private final Random random = new Random();

    private final Map<String, Set<Long>> shownProverbsMap = new HashMap<>();

    private static final List<String> SUPPORTED_LANGUAGES =
            List.of("Yoruba", "Igbo", "Hausa", "Efik", "Ibibio");

    public Proverb getRandomProverb() {
        List<Proverb> all = proverbRepository.findAll();
        if (all.isEmpty()) {
            throw new ResourceNotFoundException("No proverbs available yet! Please add some first.");
        }
        return getUniqueProverb("ALL", all);
    }
    public Proverb getRandomByLanguage(String language) {
        List<Proverb> list = proverbRepository.findByLanguageIgnoreCase(language);
        if (list.isEmpty()) {
            throw new ResourceNotFoundException("No proverb found for language: " + language);
        }
        return getUniqueProverb(language.toUpperCase(), list);
    }
    public List<Proverb> getAll() {
        List<Proverb> all = proverbRepository.findAll();
        if (all.isEmpty()) {
            throw new ResourceNotFoundException("No proverbs found in the database.");
        }
        return all;
    }
    public Proverb add(Proverb proverb) {
        if (proverb.getLanguage() == null || proverb.getProverb() == null) {
            throw new IllegalArgumentException("Language and proverb text cannot be null");
        }
        if (SUPPORTED_LANGUAGES.stream().noneMatch(lang -> lang.equalsIgnoreCase(proverb.getLanguage()))) {
            throw new IllegalArgumentException("Unsupported language. Supported: " + SUPPORTED_LANGUAGES);
        }
        return proverbRepository.save(proverb);
    }
    @Deprecated
    public Proverb getRandomProverbAcrossLanguages() {
        return getRandomProverb();
    }
    private Proverb getUniqueProverb(String key, List<Proverb> list) {
        shownProverbsMap.putIfAbsent(key, new HashSet<>());
        Set<Long> shownIds = shownProverbsMap.get(key);

        if (shownIds.size() >= list.size()) {
            logger.debug("Resetting shown proverbs list for key: {}", key);
            shownIds.clear();
        }

        List<Proverb> available = list.stream()
                .filter(p -> !shownIds.contains(p.getId()))
                .collect(Collectors.toList());

        if (available.isEmpty()) available = list;

        Proverb selected = available.get(random.nextInt(available.size()));
        shownIds.add(selected.getId());
        logger.debug("Selected proverb ID {} for key {}. Available count: {}", selected.getId(), key, available.size());

        return selected;
    }
}
