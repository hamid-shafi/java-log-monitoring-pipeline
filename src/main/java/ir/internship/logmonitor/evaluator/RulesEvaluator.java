package ir.internship.logmonitor.evaluator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.internship.logmonitor.model.LogComponent;
import ir.internship.logmonitor.model.LogEvent;
import ir.internship.logmonitor.model.LogLevel;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class RulesEvaluator {

    private String singleLogRuleName;
    private boolean singleLogEnabled;
    private LogLevel singleLogLevel;

    private void singleLogInit(JsonNode v) {
        singleLogRuleName = v.get("ruleName").asText();
        singleLogEnabled = v.get("enabled").asBoolean();
        singleLogLevel = LogLevel.valueOf(v.get("level").asText());
    }

    private String frequencyRuleName;
    private boolean frequencyEnabled;
    private LogLevel frequencyLevel;
    private int frequencyWindowSeconds;
    private int frequencyThreshold;

    private void frequencyInit(JsonNode v) {
        frequencyRuleName = v.get("ruleName").asText();
        frequencyEnabled = v.get("enabled").asBoolean();
        frequencyLevel = LogLevel.valueOf(v.get("level").asText());
        frequencyWindowSeconds = v.get("windowSeconds").asInt();
        frequencyThreshold = v.get("threshold").asInt();
    }

    private  String rateRuleName;
    private boolean rateEnabled;
    private int rateWindowSeconds;
    private int rateThreshold;

    private void rateInit(JsonNode v) {
        rateRuleName = v.get("ruleName").asText();
        rateEnabled = v.get("enabled").asBoolean();
        rateWindowSeconds = v.get("windowSeconds").asInt();
        rateThreshold = v.get("threshold").asInt();
    }


    public RulesEvaluator() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InputStream config = getClass().getClassLoader().getResourceAsStream("rulesConfig.json");
        JsonNode root = mapper.readTree(config);
        singleLogInit(root.get("singleLog"));
        frequencyInit(root.get("frequency"));
        rateInit(root.get("rate"));

        for (LogComponent component : LogComponent.values()) {
            freQs.put(component, new LinkedList<>());
            freAlertActive.put(component, false);

            rateQs.put(component, new LinkedList<>());
            rateAlertActive.put(component, false);

            EnumMap<LogLevel, Integer> e = new EnumMap<>(LogLevel.class);
            for (LogLevel l : LogLevel.values()) {
                e.put(l, 0);
            }
            rateLvlCnt.put(component, e);
        }
    }


    public void process(LogEvent event) {
        evaluateFrequencyRule(event);
        evaluateRateRule(event);
        evaluateSingleLogRule(event);
    }

    private void evaluateSingleLogRule(LogEvent event) {
        if (!singleLogEnabled || event.getLevel() != singleLogLevel)
            return;
        String ruleName = singleLogRuleName;
        String component = event.getComponent().toString();
        String level = event.getLevel().toString();
        LocalDateTime timestamp = event.getTimestamp();
        String description =
                "Single log with level " + level + " detected.\n" +
                        "Component: " + component + "\n" +
                        "Class: " + event.getClassName() + "\n" +
                        "Thread: " + event.getThreadName() + "\n" +
                        "Log Message:\n\n" +
                        event.getMessage();
        createAlert(ruleName, component, level, timestamp, description);
    }

    private final Map<LogComponent, Deque<LogEvent>> freQs = new EnumMap<>(LogComponent.class);
    private final Map<LogComponent, Boolean> freAlertActive = new EnumMap<>(LogComponent.class);

    private void evaluateFrequencyRule(LogEvent event) {
        if (!frequencyEnabled || event.getLevel() != frequencyLevel)
            return;
        String ruleName = frequencyRuleName;
        String component = event.getComponent().toString();
        String level = event.getLevel().toString();
        LocalDateTime timestamp = event.getTimestamp();

        Deque<LogEvent> freQ = freQs.get(event.getComponent());
        freQ.addLast(event);
        while (freQ.size() >= 2) {
            if (Duration.between(freQ.peekFirst().getTimestamp(), freQ.peekLast().getTimestamp()).getSeconds() > frequencyWindowSeconds) {
                freQ.removeFirst();
            }
            else break;
        }

        if (freQ.size() >= frequencyThreshold) {
            if (!freAlertActive.get(event.getComponent())) {
                String mostRepeatedClass = null;
                int maxCount = 0;
                Map<String, Integer> cnt = new HashMap<>();
                LogEvent secondLast = null;
                LogEvent last = null;

                for (LogEvent log : freQ) {
                    secondLast = last;
                    last = log;

                    cnt.merge(log.getClassName(), 1, Integer::sum);
                }

                for (Map.Entry<String, Integer> e : cnt.entrySet()) {
                    if (e.getValue() > maxCount) {
                        mostRepeatedClass = e.getKey();
                        maxCount = e.getValue();
                    }
                }

                long dur = Duration.between(
                        freQ.peekFirst().getTimestamp(),
                        freQ.peekLast().getTimestamp()
                ).getSeconds();

                double rate = dur == 0 ? freQ.size() : (double) freQ.size() / dur;
                double percent = 100.0 * maxCount / freQ.size();



                String description =
                    freQ.size() + " " + level + " logs were detected in component " +
                        component + " within " + dur + " seconds.\n" +
                        "Current rate: " + String.format("%.2f", rate) + " logs/second.\n\n" +

                        "Most frequent class:\n" +
                        mostRepeatedClass + " (" + maxCount + " logs, " +
                        String.format("%.1f", percent) + "% of all logs)\n\n" +

                        "Last two logs:\n" +
                        "[" + secondLast.getTimestamp() + "] " +
                        secondLast.getClassName() + " - " +
                        secondLast.getMessage() + "\n" +

                        "[" + last.getTimestamp() + "] " +
                        last.getClassName() + " - " +
                        last.getMessage();

                createAlert(ruleName, component, level, timestamp, description);
            }
            freAlertActive.put(event.getComponent(), true);
        }
        else {
            freAlertActive.put(event.getComponent(), false);
        }
    }

    private final Map<LogComponent, Deque<LogEvent>> rateQs = new EnumMap<>(LogComponent.class);
    private final Map<LogComponent, Boolean> rateAlertActive = new EnumMap<>(LogComponent.class);
    private final Map<LogComponent, EnumMap<LogLevel, Integer>> rateLvlCnt = new EnumMap<>(LogComponent.class);

    private void evaluateRateRule(LogEvent event) {
        if (!rateEnabled)
            return;
        String ruleName = rateRuleName;
        String component = event.getComponent().toString();
        LocalDateTime timestamp = event.getTimestamp();

        Deque<LogEvent> rateQ = rateQs.get(event.getComponent());
        rateQ.addLast(event);
        EnumMap<LogLevel, Integer> rateLC = rateLvlCnt.get(event.getComponent());
        rateLC.merge(event.getLevel(), 1, Integer::sum);
        while (rateQ.size() >= 2) {
            if (Duration.between(rateQ.peekFirst().getTimestamp(), rateQ.peekLast().getTimestamp()).getSeconds() > rateWindowSeconds) {
                rateLC.merge(rateQ.peekFirst().getLevel(), -1, Integer::sum);
                rateQ.removeFirst();
            }
            else break;
        }

        if (rateQ.size() >= rateThreshold) {
            if (!rateAlertActive.get(event.getComponent())) {
                long dur = Duration.between(
                    rateQ.peekFirst().getTimestamp(),
                    rateQ.peekLast().getTimestamp()
                ).getSeconds();

                double rate = dur == 0 ? rateQ.size() : (double) rateQ.size() / dur;

                String description =
                    rateQ.size() + " logs were detected in component " +
                        component + " within " + dur + " seconds.\n" +
                        "Current rate: " + String.format("%.2f", rate) + " logs/second.\n\n" +

                        "Log level distribution:\n" +
                        "ERROR: " + rateLC.get(LogLevel.ERROR) + "\n" +
                        "WARN : " + rateLC.get(LogLevel.WARN) + "\n" +
                        "INFO : " + rateLC.get(LogLevel.INFO) + "\n" +
                        "DEBUG: " + rateLC.get(LogLevel.DEBUG);

                createAlert(
                    ruleName,
                    component,
                    null,
                    timestamp,
                    description
                );
            }
            rateAlertActive.put(event.getComponent(), true);
        }
        else {
            rateAlertActive.put(event.getComponent(), false);
        }
    }

    private void createAlert(
            String ruleName,
            String component,
            String level,
            LocalDateTime timestamp,
            String description
    ) {
        // send data to Postgre SQL
    }

}