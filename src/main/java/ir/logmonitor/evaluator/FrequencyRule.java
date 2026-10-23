package ir.logmonitor.evaluator;

import ir.logmonitor.model.LogAlert;
import ir.logmonitor.model.LogComponent;
import ir.logmonitor.model.LogEvent;
import ir.logmonitor.model.LogLevel;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class FrequencyRule {
    private final String frequencyRuleName;
    private final LogLevel frequencyLevel;
    private final int frequencyWindowSeconds;
    private final int frequencyThreshold;

    private final Map<LogComponent, Deque<LogEvent>> freQs = new EnumMap<>(LogComponent.class);
    private final Map<LogComponent, Boolean> freAlertActive = new EnumMap<>(LogComponent.class);

    public FrequencyRule(String frequencyRuleName, LogLevel frequencyLevel, int frequencyWindowSeconds, int frequencyThreshold) {
        this.frequencyRuleName = frequencyRuleName;
        this.frequencyLevel = frequencyLevel;
        this.frequencyWindowSeconds = frequencyWindowSeconds;
        this.frequencyThreshold = frequencyThreshold;

        for (LogComponent component : LogComponent.values()) {
            freQs.put(component, new LinkedList<>());
            freAlertActive.put(component, false);
        }
    }

    public LogAlert process(LogEvent event) {
        if (event.level != frequencyLevel) {
            return null;
        }

        LogAlert alert = null;

        String component = event.component.toString();
        String level = event.level.toString();
        LocalDateTime timestamp = event.timestamp;

        Deque<LogEvent> freQ = freQs.get(event.component);
        freQ.addLast(event);
        while (freQ.size() >= 2) {
            if (Duration.between(freQ.peekFirst().timestamp, freQ.peekLast().timestamp).getSeconds() > frequencyWindowSeconds) {
                freQ.removeFirst();
            }
            else break;
        }

        if (freQ.size() >= frequencyThreshold) {
            if (!freAlertActive.get(event.component)) {
                String mostRepeatedClass = null;
                int maxCount = 0;
                Map<String, Integer> cnt = new HashMap<>();
                LogEvent secondLast = null;
                LogEvent last = null;

                for (LogEvent log : freQ) {
                    secondLast = last;
                    last = log;

                    cnt.merge(log.className, 1, Integer::sum);
                }

                for (Map.Entry<String, Integer> e : cnt.entrySet()) {
                    if (e.getValue() > maxCount) {
                        mostRepeatedClass = e.getKey();
                        maxCount = e.getValue();
                    }
                }

                long dur = Duration.between(
                        freQ.peekFirst().timestamp,
                        freQ.peekLast().timestamp
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
                                "[" + secondLast.timestamp + "] " +
                                secondLast.className + " - " +
                                secondLast.message + "\n" +

                                "[" + last.timestamp + "] " +
                                last.className + " - " +
                                last.message;

                alert = new LogAlert(frequencyRuleName, component, level, timestamp, description);
            }
            freAlertActive.put(event.component, true);
        }
        else {
            freAlertActive.put(event.component, false);
        }
        return alert;
    }
}
