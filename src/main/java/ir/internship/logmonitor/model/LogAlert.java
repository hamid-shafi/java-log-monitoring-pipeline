package ir.internship.logmonitor.model;

import java.time.LocalDateTime;

public class LogAlert {
    public final Integer id;
    public final String ruleName;
    public final String component;
    public final String level;
    public final LocalDateTime timestamp;
    public final String description;

    public LogAlert(
            String ruleName,
            String component,
            String level,
            LocalDateTime timestamp,
            String description
    ) {
        this(null, ruleName, component, level, timestamp, description);
    }

    public LogAlert(
            Integer id,
            String ruleName,
            String component,
            String level,
            LocalDateTime timestamp,
            String description
    ) {
        this.id = id;
        this.ruleName = ruleName;
        this.component = component;
        this.level = level;
        this.timestamp = timestamp;
        this.description = description;
    }

    public String toString() {
        return "LogAlert{" +
                "ruleName='" + ruleName + '\'' +
                ", component='" + component + '\'' +
                ", level='" + level + '\'' +
                ", timestamp=" + timestamp +
                ", description='" + description + '\'' +
                '}';
    }
}
