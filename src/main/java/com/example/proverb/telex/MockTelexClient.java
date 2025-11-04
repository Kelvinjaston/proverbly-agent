package com.example.proverb.telex;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MockTelexClient implements TelexClient{
    private static final Logger logger = LoggerFactory.getLogger(MockTelexClient.class);
    @Override
    public boolean sendMessage(String message) {

        logger.info("\n---  MOCK TELEX MESSAGE SENT ---\n" +
                "Destination: #DailyProverbsChannel\n" +
                "Payload: \n{}\n" +
                "-----------------------------------\n", message);
        return true;
    }
}
