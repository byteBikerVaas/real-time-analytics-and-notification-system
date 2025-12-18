package com.example.realtime.processor.service;

import com.example.realtime.common.dto.EventDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaConsumerService.class);

    // 1. The Tool: Redis Client for Strings
    private final StringRedisTemplate redisTemplate;

    // 2. The Topic to publish to (injected from RedisConfig)
    private final ChannelTopic topic;

    // Constructor Injection
    @Autowired
    public KafkaConsumerService(StringRedisTemplate redisTemplate, ChannelTopic topic) {
        this.redisTemplate = redisTemplate;
        this.topic = topic;
    }

    @KafkaListener(topics = "${app.kafka.topic}", groupId = "realtime-analytics-group")
    public void consumeEvents(EventDTO event) {
        logger.info("Consumed event: userId={}, type={}", event.getUserId(), event.getEventType());

        // 2. Define the Key
        String key = "stats:" + event.getUserId() + ":" + event.getEventType();

        // 3. The Logic: Increment counter in Redis
        redisTemplate.opsForValue().increment(key);

        // Optional: Print the new count to verify it works
        String newCount = redisTemplate.opsForValue().get(key);
        logger.info("Updated Redis Key: {} -> Count: {}", key, newCount);

        // CHANGE: Publish to Redis pub/sub channel so Gateway instances can receive it
        String messageToSend = event.toString(); // TODO: replace with JSON serialization if needed
        redisTemplate.convertAndSend(topic.getTopic(), messageToSend);
        logger.info("Published event to Redis topic {}", topic.getTopic());
    }
}