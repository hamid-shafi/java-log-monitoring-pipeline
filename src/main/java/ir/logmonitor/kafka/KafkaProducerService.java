package ir.logmonitor.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.Properties;

public class KafkaProducerService {
    private final KafkaProducer<String, String> producer;

    public KafkaProducerService() {
        Properties pro = new Properties();
        String bootstrapServers = System.getenv("KAFKA_BOOTSTRAP_SERVERS");
        pro.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers == null || bootstrapServers.isBlank() ? "localhost:9092" : bootstrapServers);
        pro.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        pro.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        producer = new KafkaProducer<>(pro);
    }

    public void send(String key, String value) {
        try {
            RecordMetadata md = producer.send(
                    new ProducerRecord<>(KafkaTopics.LOGS, key, value)
            ).get();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void close() {
        producer.close();
    }
}
