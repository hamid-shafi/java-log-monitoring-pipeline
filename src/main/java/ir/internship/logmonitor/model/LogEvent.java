package ir.internship.logmonitor.model;

import java.time.LocalDateTime;

public class LogEvent {
    private final LocalDateTime timestamp;
    private final LogComponent component;
    private final LogLevel level;
    private final String threadName;
    private final String className;
    private final String message;
    private final String fileName;

    public LogEvent(
            LocalDateTime timestamp,
            LogComponent component,
            LogLevel level,
            String threadName,
            String className,
            String message,
            String fileName
    ) {
        this.timestamp = timestamp;
        this.component = component;
        this.level = level;
        this.threadName = threadName;
        this.className = className;
        this.message = message;
        this.fileName = fileName;
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

    public String getFileName() {
        return fileName;
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
                ", fileName='" + fileName + '\'' +
                '}';
    }
}
