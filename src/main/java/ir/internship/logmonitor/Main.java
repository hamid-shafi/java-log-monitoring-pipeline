package ir.internship.logmonitor;

import ir.internship.logmonitor.api.Api;
import ir.internship.logmonitor.ingester.FileIngester;
import ir.internship.logmonitor.kafka.KafkaConsumerService;

import java.io.IOException;
import java.sql.SQLException;

public class Main {
    public static void main(String[] args) throws Exception {
        FileIngester ingester = new FileIngester();
        KafkaConsumerService consumer = new KafkaConsumerService();
        Api api = new Api();

        Thread ingesterThread = new Thread(new Runnable() {
            public void run() {
                try {
                    ingester.start();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Thread kafkaConsumerThread = new Thread(new Runnable() {
            public void run() {
                try {
                    consumer.consume();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                System.out.println("Shutting down...");
                ingester.close();
                consumer.close();
                try {
                    api.close();
                }
                catch (Exception e) {}
            }
        }));
        ingesterThread.start();
        kafkaConsumerThread.start();
        api.start();
    }
}