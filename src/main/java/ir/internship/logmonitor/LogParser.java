package ir.internship.logmonitor;
import ir.internship.logmonitor.model.LogComponent;
import ir.internship.logmonitor.model.LogEvent;
import ir.internship.logmonitor.model.LogLevel;
import java.time.LocalDateTime;

public class LogParser {
    public static LogComponent getComponentName(String logFileName) {
        logFileName = logFileName.substring(0, logFileName.lastIndexOf('.'));
        String fileTimestampSample = "YYYY-MM-DD_HH-MM-SS";
        int lenOfFileTimestamp = fileTimestampSample.length();
        String componentName = logFileName.substring(0, logFileName.length()-lenOfFileTimestamp+1);
        componentName = componentName.toUpperCase();
        return LogComponent.valueOf(componentName);
    }

    public static LogEvent parse(LogComponent component, String line) {
        // line format: 2021-07-12 01:22:42,114 [ThreadName] INFO package.name.ClassName - msg
        LocalDateTime timestamp;
        int year, month, day, hour, minute, second, nano;
        year = Integer.parseInt(line.substring(0, line.indexOf('-')));
        line = line.substring(line.indexOf('-') + 1);

        month = Integer.parseInt(line.substring(0, line.indexOf('-')));
        line = line.substring(line.indexOf('-') + 1);

        day = Integer.parseInt(line.substring(0, line.indexOf(' ')));
        line = line.substring(line.indexOf(' ') + 1);

        hour = Integer.parseInt(line.substring(0, line.indexOf(':')));
        line = line.substring(line.indexOf(':') + 1);

        minute = Integer.parseInt(line.substring(0, line.indexOf(':')));
        line = line.substring(line.indexOf(':') + 1);

        second = Integer.parseInt(line.substring(0, line.indexOf(',')));
        line = line.substring(line.indexOf(',') + 1);

        nano = Integer.parseInt(line.substring(0, line.indexOf(' '))) * 1000000;
        line = line.substring(line.indexOf(' ') + 1);

        timestamp = LocalDateTime.of(year, month, day, hour, minute, second, nano);

        String threadName = line.substring(1, line.indexOf(' ')-1); // [ ] ×
        line = line.substring(line.indexOf(' ') + 1);

        LogLevel level = LogLevel.valueOf(line.substring(0, line.indexOf(' ')));
        line = line.substring(line.indexOf(' ') + 1);

        String className = line.substring(0, line.indexOf(' '));
        line = line.substring(line.indexOf(' '));
        // remove " - "
        line = line.substring(3);

        String message = line;

        return new LogEvent(
                timestamp,
                component,
                level,
                threadName,
                className,
                message
        );
    }
}
