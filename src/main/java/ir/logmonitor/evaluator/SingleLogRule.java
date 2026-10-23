package ir.logmonitor.evaluator;

import ir.logmonitor.model.LogAlert;
import ir.logmonitor.model.LogEvent;
import ir.logmonitor.model.LogLevel;

import java.time.LocalDateTime;

public class SingleLogRule {
    private final LogLevel singleLogLevel;
    private final String singleLogRuleName;

    public SingleLogRule(String singleLogRuleName, LogLevel singleLogLevel) {
        this.singleLogRuleName = singleLogRuleName;
        this.singleLogLevel = singleLogLevel;
    }

    public LogAlert process(LogEvent event) {
        if (event.level != singleLogLevel)
            return null;
        String component = event.component.toString();
        String level = event.level.toString();
        LocalDateTime timestamp = event.timestamp;
        String description =
                "Single log with level " + level + " detected.\n" +
                        "Component: " + component + "\n" +
                        "Class: " + event.className + "\n" +
                        "Thread: " + event.threadName + "\n" +
                        "Log Message:\n\n" +
                        event.message;
        return new LogAlert(singleLogRuleName, component, level, timestamp, description);
    }
}