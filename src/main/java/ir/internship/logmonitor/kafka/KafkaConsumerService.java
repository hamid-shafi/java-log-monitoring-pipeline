package ir.internship.logmonitor.kafka;

import ir.internship.logmonitor.LogParser;
import ir.internship.logmonitor.evaluator.RulesEvaluator;
import ir.internship.logmonitor.model.LogComponent;
import ir.internship.logmonitor.model.LogEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Properties;

public class KafkaConsumerService {
    private final KafkaConsumer<String, String> consumer;

    public KafkaConsumerService() {
        Properties pro = new Properties();
        pro.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        pro.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        pro.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        pro.put(ConsumerConfig.GROUP_ID_CONFIG, "rules-evaluator");
        consumer = new KafkaConsumer<>(pro);
        consumer.subscribe(List.of(KafkaTopics.LOGS));
    }

    public void consume() throws IOException {
        RulesEvaluator evaluator = new RulesEvaluator();
        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(100));
            for (ConsumerRecord<String, String> record : records) {
                LogEvent event = LogParser.parse(LogComponent.valueOf(record.key()), record.value());
                evaluator.process(event);
            }
        }
    }

    public void close() { consumer.close(); }
}

