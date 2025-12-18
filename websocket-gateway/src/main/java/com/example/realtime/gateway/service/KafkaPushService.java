package com.example.realtime.gateway.service;

import com.example.realtime.common.dto.AggregateDTO;
import com.example.realtime.gateway.handler.EventWebSocketHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaPushService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaPushService.class);

    private final EventWebSocketHandler webSocketHandler;
    private final ObjectMapper objectMapper;

    public KafkaPushService(EventWebSocketHandler webSocketHandler, ObjectMapper objectMapper) {
        this.webSocketHandler = webSocketHandler;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "events-aggregated", groupId = "websocket-push-group")
    public void consumeAggregates(AggregateDTO aggregate) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(aggregate);
            String userId = aggregate.getMetricId();

            logger.debug("Received aggregate for user {}: {}", userId, jsonMessage);

            // Async send to the specific user
            webSocketHandler.sendToUserAsync(userId, jsonMessage);
        } catch (Exception e) {
            logger.error("Error processing aggregate message", e);
        }
    }
}