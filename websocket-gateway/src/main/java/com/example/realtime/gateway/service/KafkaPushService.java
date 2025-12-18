package com.example.realtime.gateway.service;

import com.example.realtime.common.dto.EventDTO;
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

    @KafkaListener(topics = "${app.kafka.topic}", groupId = "websocket-push-group")
    public void consumeEvents(EventDTO event) {
        try {
            // Convert the event to JSON
            String jsonMessage = objectMapper.writeValueAsString(event);
            
            logger.info("Pushing event to frontend: {}", jsonMessage);
            
            // Broadcast to all connected users
            webSocketHandler.broadcast(jsonMessage);
        } catch (Exception e) {
            logger.error("Error broadcasting event", e);
        }
    }
}