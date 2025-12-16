package com.example.realtime.ingest.service;

import com.example.realtime.common.dto.EventDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducerService {

    // 1. The Tool: KafkaTemplate
    // <String, EventDTO> means: Key is a String, Value is our Event object.
    private final KafkaTemplate<String, EventDTO> kafkaTemplate;

    // 2. The Address: Topic Name
    // We will load this from application.yml
    private final String topicName;

    // Constructor Injection: Spring provides the tools here
    public KafkaProducerService(KafkaTemplate<String, EventDTO> kafkaTemplate,
                                @Value("${app.kafka.topic}") String topicName) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }

    public void sendEvent(EventDTO event) {
        // Logic: Send the event to the topic.
        // We use the userId as the "Key". This ensures all events for the same user
        // go to the same partition (Order Guarantee).
        kafkaTemplate.send(topicName, event.getUserId(), event);
        
        // Simple logging to prove it worked
        System.out.println("Message sent to Kafka topic: " + topicName);
    }
}