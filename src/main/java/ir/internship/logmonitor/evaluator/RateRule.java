package ir.internship.logmonitor.evaluator;

import ir.internship.logmonitor.model.LogAlert;
import ir.internship.logmonitor.model.LogComponent;
import ir.internship.logmonitor.model.LogEvent;
import ir.internship.logmonitor.model.LogLevel;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Deque;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Map;

public class RateRule {
    private final String rateRuleName;
    private final int rateWindowSeconds;
    private final int rateThreshold;

    private final Map<LogComponent, Deque<LogEvent>> rateQs = new EnumMap<>(LogComponent.class);
    private final Map<LogComponent, Boolean> rateAlertActive = new EnumMap<>(LogComponent.class);
    private final Map<LogComponent, EnumMap<LogLevel, Integer>> rateLvlCnt = new EnumMap<>(LogComponent.class);

    public RateRule(String rateRuleName, int rateWindowSeconds, int rateThreshold) {
        this.rateRuleName = rateRuleName;
        this.rateWindowSeconds = rateWindowSeconds;
        this.rateThreshold = rateThreshold;

        for (LogComponent component : LogComponent.values()) {
            rateQs.put(component, new LinkedList<>());
            rateAlertActive.put(component, false);

            EnumMap<LogLevel, Integer> e = new EnumMap<>(LogLevel.class);
            for (LogLevel l : LogLevel.values()) {
                e.put(l, 0);
            }
            rateLvlCnt.put(component, e);
        }
    }

    public LogAlert process(LogEvent event) {
        LogAlert alert = null;

        String ruleName = rateRuleName;
        String component = event.component.toString();
        LocalDateTime timestamp = event.timestamp;

        Deque<LogEvent> rateQ = rateQs.get(event.component);
        rateQ.addLast(event);
        EnumMap<LogLevel, Integer> rateLC = rateLvlCnt.get(event.component);
        rateLC.merge(event.level, 1, Integer::sum);
        while (rateQ.size() >= 2) {
            if (Duration.between(rateQ.peekFirst().timestamp, rateQ.peekLast().timestamp).getSeconds() > rateWindowSeconds) {
                rateLC.merge(rateQ.peekFirst().level, -1, Integer::sum);
                rateQ.removeFirst();
            }
            else break;
        }

        if (rateQ.size() >= rateThreshold) {
            if (!rateAlertActive.get(event.component)) {
                long dur = Duration.between(
                        rateQ.peekFirst().timestamp,
                        rateQ.peekLast().timestamp
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

                alert = new LogAlert(ruleName, component, null, timestamp, description);
            }
            rateAlertActive.put(event.component, true);
        }
        else {
            rateAlertActive.put(event.component, false);
        }
        return alert;
    }
}
