package ir.internship.logmonitor.model;

import java.time.LocalDateTime;

public class LogEvent {
    private final LocalDateTime timestamp;
    private final LogComponent component;
    private final LogLevel level;
    private final String threadName;
    private final String className;
    private final String message;

    public LogEvent(
            LocalDateTime timestamp,
            LogComponent component,
            LogLevel level,
            String threadName,
            String className,
            String message
    ) {
        this.timestamp = timestamp;
        this.component = component;
        this.level = level;
        this.threadName = threadName;
        this.className = className;
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public LogComponent getComponent() {
        return component;
    }

    public LogLevel getLevel() {
        return level;
    }

    public String getThreadName() {
        return threadName;
    }

    public String getClassName() {
        return className;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "LogEvent{" +
                "timestamp=" + timestamp +
                ", component=" + component +
                ", level=" + level +
                ", threadName='" + threadName + '\'' +
                ", className='" + className + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
