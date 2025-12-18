package com.example.realtime.gateway.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class EventWebSocketHandler extends TextWebSocketHandler {

    private static final Logger logger = LoggerFactory.getLogger(EventWebSocketHandler.class);

    // CHANGE 1: Map of userId -> WebSocketSession
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    // Virtual thread executor for asynchronous send operations
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        // CHANGE 2: Extract userId from query param: ?userId=alex_doe
        String userId = extractUserId(session.getUri());
        if (userId != null && !userId.isEmpty()) {
            sessions.put(userId, session);
            logger.info("New WebSocket connection for user {}: sessionId={}", userId, session.getId());
        } else {
            // Fallback to session id as key if no userId provided
            sessions.put(session.getId(), session);
            logger.info("New WebSocket connection with no userId: sessionId={}", session.getId());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // Remove the session from the map by value
        sessions.values().remove(session);
        logger.info("WebSocket connection closed: {}", session.getId());
    }

    // CHANGE 3: Send message to a specific user
    public void sendToUser(String userId, String message) {
        WebSocketSession session = sessions.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                logger.error("Error sending message to user {}: {}", userId, e.getMessage());
            }
        } else {
            logger.warn("No open session for user {}", userId);
        }
    }

    // Asynchronous, virtual-thread backed send
    public void sendToUserAsync(String userId, String message) {
        WebSocketSession session = sessions.get(userId);
        if (session == null) {
            logger.warn("No session found for user {} when attempting async send", userId);
            return;
        }

        executor.submit(() -> {
            if (!session.isOpen()) {
                logger.warn("Session for user {} is closed, dropping message", userId);
                return;
            }

            synchronized (session) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    logger.warn("Dropping message for user {} due to IOException: {}", userId, e.getMessage());
                }
            }
        });
    }

    // Keep a broadcast method for backwards compatibility
    public void broadcast(String message) {
        for (WebSocketSession session : sessions.values()) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    logger.error("Error sending message to session {}", session.getId(), e);
                }
            }
        }
    }

    private String extractUserId(URI uri) {
        if (uri == null || uri.getQuery() == null) return null;
        String[] parts = uri.getQuery().split("&");
        for (String p : parts) {
            String[] kv = p.split("=", 2);
            if (kv.length == 2 && "userId".equalsIgnoreCase(kv[0])) {
                return kv[1];
            }
        }
        return null;
    }

    // Shutdown executor when handler is GC'd - best-effort
    @Override
    protected void finalize() throws Throwable {
        try {
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } finally {
            super.finalize();
        }
    }
}