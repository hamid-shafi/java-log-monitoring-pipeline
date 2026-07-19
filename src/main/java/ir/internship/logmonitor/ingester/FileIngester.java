package ir.internship.logmonitor.ingester;

import ir.internship.logmonitor.model.LogEvent;

import java.io.File;
import java.io.InterruptedIOException;
import java.nio.file.*;
import java.util.List;
import java.io.IOException;

public class FileIngester {
    public void processFile(File file) throws IOException {
        List<String> lines = Files.readAllLines(file.toPath());
        for (String line : lines) {
            LogEvent logEvent = LogParser.parse(file.getName(), line);
            // Kafka
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
        while (true) {
            File[] files = new File("logs").listFiles();
            if (files != null) {
                for (File file : files) {
                    processFile(file);
                }
            }
            WatchKey key = watchService.take();

            for (WatchEvent<?> event : key.pollEvents()) {
                Path fileName = (Path)event.context();
                processFile(new File("log", fileName.toString()));
            }

            key.reset();
        }
    }
}
