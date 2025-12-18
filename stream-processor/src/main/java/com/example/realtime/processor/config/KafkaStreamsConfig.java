package com.example.realtime.processor.config;

import com.example.realtime.common.dto.EventDTO;
import com.example.realtime.common.dto.AggregateDTO;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerde;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafkaStreams
public class KafkaStreamsConfig {

    @Bean(name = "eventJsonSerde")
    public Serde<EventDTO> eventJsonSerde() {
        JsonSerde<EventDTO> serde = new JsonSerde<>(EventDTO.class);
        return serde;
    }

    @Bean(name = "aggregateJsonSerde")
    public Serde<AggregateDTO> aggregateJsonSerde() {
        JsonSerde<AggregateDTO> serde = new JsonSerde<>(AggregateDTO.class);
        return serde;
    }

    @Bean(name = "defaultKafkaStreamsConfig")
    public KafkaStreamsConfiguration defaultKafkaStreamsConfig() {
        Map<String, Object> props = new HashMap<>();
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "stream-processor-streams-app");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new KafkaStreamsConfiguration(props);
    }
}
