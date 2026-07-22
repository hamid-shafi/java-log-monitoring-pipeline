package ir.internship.logmonitor.model;

import java.time.LocalDateTime;

public class LogEvent {
    public final LocalDateTime timestamp;
    public final LogComponent component;
    public final LogLevel level;
    public final String threadName;
    public final String className;
    public final String message;

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
