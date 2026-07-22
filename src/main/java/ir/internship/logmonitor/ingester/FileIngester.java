package ir.internship.logmonitor.ingester;

import ir.internship.logmonitor.LogParser;
import ir.internship.logmonitor.kafka.KafkaProducerService;

import java.io.File;
import java.nio.file.*;
import java.util.List;
import java.io.IOException;

public class FileIngester {
    private final KafkaProducerService pro = new KafkaProducerService();
    public volatile boolean running = true;

    public void processFile(File file) throws IOException {
        List<String> lines = Files.readAllLines(file.toPath());
        for (String line : lines) {
            String componentName = LogParser.getComponentName(file.getName()).name();
            pro.send(componentName, line);
        }
        if (!(file.delete())) {
            System.out.println("ERR: Failed to delete " + file.getName());
        }
    }

    public void start() throws IOException, InterruptedException {
        WatchService watchService = FileSystems.getDefault().newWatchService();
        Paths.get("logs").register(
                watchService,
                StandardWatchEventKinds.ENTRY_CREATE
        );
        while (running) {
            File[] files = new File("logs").listFiles();
            if (files != null) {
                for (File file : files) {
                    processFile(file);
                }
            }
            WatchKey key = watchService.take();

            for (WatchEvent<?> event : key.pollEvents()) {
                Path fileName = (Path)event.context();
                processFile(new File("logs", fileName.toString()));
            }

            key.reset();
        }
    }

    public void close() {
        running = false;
        pro.close();
    }
}
