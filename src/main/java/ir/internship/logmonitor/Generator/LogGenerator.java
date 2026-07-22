package ir.internship.logmonitor.Generator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class LogGenerator {
    private final ObjectMapper mapper = new ObjectMapper();

    private int files;
    private int totalLogs;
    private int durationSeconds;
    private boolean addBackupLogs;
    private long seed;

    private List<String> levels;
    private List<String> components;
    private List<String> classNames;

    private Random random;
    private long logId;

    private final DateTimeFormatter fileFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private final DateTimeFormatter logFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS");

    public void generate() throws IOException {
        loadConfig();
        random = new Random(seed);
        logId = 0;

        if (addBackupLogs)
            addBackupFiles();

        generateFiles();
    }

    private void loadConfig() throws IOException {
        try (InputStream config = getClass().getClassLoader()
                .getResourceAsStream("generatorConfig.json")) {

            if (config == null)
                throw new FileNotFoundException("generatorConfig.json not found");

            JsonNode root = mapper.readTree(config);

            files = root.get("files").asInt();
            totalLogs = root.get("totalLogs").asInt();
            durationSeconds = root.get("durationSeconds").asInt();
            addBackupLogs = root.get("addBackupLogs").asBoolean();
            seed = root.get("seed").asLong();

            levels = readList(root.get("levels"));
            components = readList(root.get("components"));
            classNames = readList(root.get("classNames"));
        }
    }

    private List<String> readList(JsonNode node) {
        List<String> result = new ArrayList<>();
        for (JsonNode x : node)
            result.add(x.asText());
        return result;
    }

    private void generateFiles() throws IOException {
        File dir = new File("logs");
        dir.mkdirs();

        // Remove old logs
        File[] oldFiles = dir.listFiles();
        if (oldFiles != null) {
            for (File file : oldFiles)
                file.delete();
        }

        LocalDateTime start = LocalDateTime.now();

        List<LocalDateTime> timestamps = new ArrayList<>(totalLogs);

        long durationMilli = durationSeconds * 1000L;

        for (int i = 0; i < totalLogs; i++) {
            long offset = random.nextLong(durationMilli);
            timestamps.add(start.plusNanos(offset * 1000000L));
        }

        timestamps.sort(Comparator.naturalOrder());

        int[] fileSizes = randomPartition(totalLogs, files);

        List<String> fileComponents = new ArrayList<>();
        for (int i = 0; i < files; i++)
            fileComponents.add(randomComponent());

        int index = 0;
        for (int fileId = 0; fileId < files; fileId++) {

            int size = fileSizes[fileId];

            File file = new File(
                    dir,
                    fileComponents.get(fileId)
                            + timestamps.get(index).format(fileFormatter)
                            + ".log"
            );
            try (FileWriter writer = new FileWriter(file)) {
                for (int line = 1; line <= size; line++) {
                    writer.write(generateLog(
                            timestamps.get(index),
                            fileId,
                            line
                    ));
                    writer.write('\n');
                    index++;
                }
            }
        }
    }

    private String generateLog(LocalDateTime time, int fileId, int line) {
        long id = ++logId;

        String level = randomLevel();
        String className = randomClass();
        String thread = "thread-" + random.nextInt(5);

        String message =
                "Log ID=" + id +
                        ", file=" + fileId +
                        ", line=" + line +
                        ", generated test message";

        return String.format(
                "%s [%s] %s %s - %s",
                time.format(logFormatter),
                thread,
                level,
                className,
                message
        );
    }
    private int[] randomPartition(int total, int parts) {
        if (parts == 1)
            return new int[]{total};

        int[] result = new int[parts];

        Arrays.fill(result, total / parts);

        for (int i = 0; i < total % parts; i++)
            result[i]++;

        int moves = total;

        for (int i = 0; i < moves; i++) {
            int from = random.nextInt(parts);
            int to = random.nextInt(parts);

            if (from == to || result[from] <= 1)
                continue;

            result[from]--;
            result[to]++;
        }

        return result;
    }

    private String randomLevel() {
        return levels.get(random.nextInt(levels.size()));
    }

    private String randomComponent() {
        return components.get(random.nextInt(components.size()));
    }

    private String randomClass() {
        return classNames.get(random.nextInt(classNames.size()));
    }

    private void addBackupFiles() throws IOException {
        File backup = new File("logs-backup");
        File logs = new File("logs");

        if (!backup.exists())
            return;

        File[] files = backup.listFiles();

        if (files == null)
            return;

        for (File file : files) {
            Files.copy(
                    file.toPath(),
                    new File(logs, file.getName()).toPath(),
                    StandardCopyOption.REPLACE_EXISTING
            );
        }

        System.out.println("Backup logs copied.");
    }
}