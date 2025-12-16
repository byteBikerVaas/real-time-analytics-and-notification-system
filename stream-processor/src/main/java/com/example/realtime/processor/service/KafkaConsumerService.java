package com.example.realtime.processor.service;

import com.example.realtime.common.dto.EventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);

    // 1. The Tool: Redis Client for Strings
    private final StringRedisTemplate redisTemplate;

    // Constructor Injection
    public KafkaConsumerService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @KafkaListener(topics = "${app.kafka.topic}", groupId = "realtime-analytics-group")
    public void consumeEvents(EventDTO event) {
        logger.info("Consumed event: userId={}, type={}", event.getUserId(), event.getEventType());

        // 2. Define the Key
        // Pattern: "stats:USER_ID:EVENT_TYPE" -> e.g., "stats:danish_j:CLICK"
        String key = "stats:" + event.getUserId() + ":" + event.getEventType();

        // 3. The Logic: Increment counter in Redis
        // opsForValue() allows us to work with simple values (Strings/Numbers)
        redisTemplate.opsForValue().increment(key);

        // Optional: Print the new count to verify it works
        String newCount = redisTemplate.opsForValue().get(key);
        logger.info("Updated Redis Key: {} -> Count: {}", key, newCount);
    }
}