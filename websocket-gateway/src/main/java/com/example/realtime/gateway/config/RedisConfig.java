package com.example.realtime.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.listener.ChannelTopic;

@Configuration
public class RedisConfig {

    // This creates the "frequency" named "live-updates"
    @Bean
    public ChannelTopic topic() {
        return new ChannelTopic("live-updates");
    }
}
