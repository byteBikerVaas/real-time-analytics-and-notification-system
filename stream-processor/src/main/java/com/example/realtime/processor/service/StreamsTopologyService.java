package com.example.realtime.processor.service;

import com.example.realtime.common.dto.AggregateDTO;
import com.example.realtime.common.dto.EventDTO;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Materialized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import org.apache.kafka.streams.KeyValue;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class StreamsTopologyService {

    private static final Logger log = LoggerFactory.getLogger(StreamsTopologyService.class);

    @Autowired
    private StreamsBuilder streamsBuilder;

    @Autowired
    private Serde<EventDTO> eventJsonSerde;

    @Autowired
    private Serde<AggregateDTO> aggregateJsonSerde;

    // Benchmarking fields
    private final AtomicLong totalProcessed = new AtomicLong(0);
    private final long startTime = System.currentTimeMillis();

    @PostConstruct
    public void buildTopology() {
        KStream<String, EventDTO> events = streamsBuilder.stream(
                "events",
                Consumed.with(Serdes.String(), eventJsonSerde)
        );

        TimeWindows windows = TimeWindows.ofSizeWithNoGrace(Duration.ofMinutes(1));

        KTable<Windowed<String>, Long> counts = events
                // --- SPEEDOMETER START ---
                .peek((key, value) -> {
                    long currentCount = totalProcessed.incrementAndGet();
                    // Log progress every 10,000 events
                    if (currentCount % 10000 == 0) {
                        long elapsedSeconds = (System.currentTimeMillis() - startTime) / 1000;
                        if (elapsedSeconds > 0) {
                            long eventsPerSec = currentCount / elapsedSeconds;
                            long eventsPerMin = eventsPerSec * 60;
                            log.info("🚀 THROUGHPUT: Processed {} events in {}s. Rate: {} events/sec (~{} events/min)",
                                    currentCount, elapsedSeconds, eventsPerSec, eventsPerMin);
                        }
                    }
                })
                // --- SPEEDOMETER END ---
                .groupBy((key, value) -> value.getUserId(), Grouped.with(Serdes.String(), eventJsonSerde))
                .windowedBy(windows)
                .count(Materialized.with(Serdes.String(), Serdes.Long()));

        KStream<String, AggregateDTO> aggregatedStream = counts
                .toStream()
                .map((windowedKey, count) -> {
                    String userId = windowedKey.key();
                    long windowStartMs = windowedKey.window().start();
                    long windowEndMs = windowedKey.window().end();
                    AggregateDTO agg = new AggregateDTO();
                    agg.setMetricId(userId);
                    agg.setWindowStart(Instant.ofEpochMilli(windowStartMs));
                    agg.setWindowEnd(Instant.ofEpochMilli(windowEndMs));
                    agg.setCount(count);
                    return KeyValue.pair(userId, agg);
                });

        aggregatedStream.to("events-aggregated", Produced.with(Serdes.String(), aggregateJsonSerde));

        log.info("Streams topology for events -> events-aggregated registered.");
    }
}