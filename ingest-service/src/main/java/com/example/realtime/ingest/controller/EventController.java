package com.example.realtime.ingest.controller;

import com.example.realtime.common.dto.EventDTO;
import com.example.realtime.ingest.service.KafkaProducerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/events")
public class EventController {

    // 1. Create the Logger
    private static final Logger logger = LoggerFactory.getLogger(EventController.class);

    private final KafkaProducerService producerService;

    public EventController(KafkaProducerService producerService) {
        this.producerService = producerService;
    }

    @PostMapping
    public ResponseEntity<Void> postEvent(@RequestBody EventDTO event) {
        // 2. Log the receipt (good for debugging)
        logger.info("Received event request: userId={}, type={}", event.getUserId(), event.getEventType());

        // 3. Send to Kafka
        producerService.sendEvent(event);

        // 4. Return "202 Accepted"
        return ResponseEntity.accepted().build();
    }
}