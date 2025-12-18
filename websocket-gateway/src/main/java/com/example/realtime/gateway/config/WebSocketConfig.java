package com.example.realtime.gateway.config;

import com.example.realtime.gateway.handler.EventWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(eventWebSocketHandler(), "/ws/events")
                .setAllowedOrigins("*"); // Important for testing!
    }

    @Bean
    public EventWebSocketHandler eventWebSocketHandler() {
        return new EventWebSocketHandler();
    }
}