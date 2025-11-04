package com.example.proverb.scheduler;

import com.example.proverb.model.Proverb;
import com.example.proverb.repo.ProverbRepository;
import com.example.proverb.service.ProverbService;
import com.example.proverb.telex.TelexClient;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class ProverbScheduler {

    private final ProverbService proverbService;
    private final ProverbRepository proverbRepository;
    private final TelexClient telexClient;

    private static final Logger logger = LoggerFactory.getLogger(ProverbScheduler.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("hh:mm a");
    private final Random random = new Random();

    private static final String[] CAPTIONS = {
            " Remember: Growth starts with gratitude.",
            " Keep learning — every day is a new chapter.",
            " Your progress may be slow, but it’s still progress.",
            " Start your day with purpose and end it with peace.",
            " Let wisdom guide your path, not haste.",
            " The small steps today build the greatness of tomorrow.",
            " Stay humble, stay hungry, and keep moving.",
            " Light up someone’s day — kindness costs nothing.",
            " Reflection brings clarity; patience brings peace.",
            " Dreams don’t work unless you do."
    };
    @Scheduled(cron = "0 0 9 * * *")
    public void sendMorningInspiration() {
        sendProverbMessage("MORNING");
    }

    @Scheduled(cron = "0 0 13 * * *")
    public void sendAfternoonWisdom() {
        sendProverbMessage("AFTERNOON");
    }
    @Scheduled(cron = "0 0 20 * * *")
    public void sendEveningReflection() {
        sendProverbMessage("EVENING");
    }
    private void sendProverbMessage(String session) {
        logger.info("{} Scheduler triggered at {}", session, LocalTime.now().format(FORMATTER));

        try {
            List<Proverb> allProverbs = proverbRepository.findAll();
            if (allProverbs.isEmpty()) {
                logger.warn(" No proverbs found in database.");
                return;
            }
            List<Proverb> localProverbs = allProverbs.stream()
                    .filter(p -> p.getMeaning() != null && !p.getMeaning().toLowerCase().contains("imported quote"))
                    .toList();

            List<Proverb> globalQuotes = allProverbs.stream()
                    .filter(p -> p.getMeaning() != null && p.getMeaning().toLowerCase().contains("imported quote"))
                    .toList();

            Proverb selected;
            switch (session) {
                case "MORNING" -> selected = globalQuotes.isEmpty() ? randomSelect(allProverbs) : randomSelect(globalQuotes);
                case "AFTERNOON" -> selected = localProverbs.isEmpty() ? randomSelect(allProverbs) : randomSelect(localProverbs);
                case "EVENING" -> selected = randomSelect(allProverbs);
                default -> {
                    logger.error(" Invalid session: {}", session);
                    return;
                }
            }
            boolean isGlobal = selected.getMeaning().toLowerCase().contains("imported quote");
            String header = switch (session) {
                case "MORNING" -> isGlobal ? " Morning Inspiration Quote" : " Morning Wisdom";
                case "AFTERNOON" -> isGlobal ? "☀ Afternoon Inspiration" : " Afternoon Nigerian Proverb";
                case "EVENING" -> isGlobal ? " Evening Global Reflection" : "🪶 Evening Local Reflection";
                default -> " Daily Proverb";
            };

            String caption = CAPTIONS[random.nextInt(CAPTIONS.length)];

            String message = String.format(
                    "%s\n\n**Language:** %s\n**Proverb:** %s\n**Meaning:** %s\n\n Time: %s\n\n%s",
                    header,
                    selected.getLanguage(),
                    selected.getProverb(),
                    selected.getMeaning(),
                    LocalTime.now().format(FORMATTER),
                    caption
            );
            boolean success = telexClient.sendMessage(message);

            if (success) {
                logger.info("[{}] Message sent successfully ({})", session, isGlobal ? "Global" : "Local");
            } else {
                logger.warn(" [{}] Message failed to send.", session);
            }

        } catch (Exception e) {
            logger.error(" Error during [{}] message send: {}", session, e.getMessage());
        }
    }
    private Proverb randomSelect(List<Proverb> list) {
        return list.get(random.nextInt(list.size()));
    }
}
