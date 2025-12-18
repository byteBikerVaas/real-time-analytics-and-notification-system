package com.example.realtime.ingest;

import com.example.realtime.common.dto.EventDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.Instant;
import java.util.stream.IntStream;

@SpringBootTest
public class LoadSimulatorTest {

    @Autowired
    private KafkaTemplate<String, EventDTO> kafkaTemplate;

    @Test
    public void simulateTrafficSpike() {
        String userId = "user_benchmark";
        // INCREASED TO 100,000
        int messageCount = 100000;

        System.out.println("🚀 STARTING BENCHMARK: Sending " + messageCount + " events...");
        long start = System.currentTimeMillis();

        IntStream.range(0, messageCount).forEach(i -> {
            EventDTO event = new EventDTO("click", userId, Instant.now(), "meta-" + i);
            kafkaTemplate.send("events", userId, event);
        });

        long end = System.currentTimeMillis();
        long duration = end - start;
        
        // Avoid divide by zero if it's super fast
        double seconds = duration / 1000.0;
        double rate = (seconds > 0) ? (messageCount / seconds) : messageCount;

        System.out.println("✅ LOAD TEST COMPLETE: Sent " + messageCount + " events in " + duration + "ms");
        System.out.println("⚡ PRODUCER RATE: " + (int)rate + " events/sec");
    }
}