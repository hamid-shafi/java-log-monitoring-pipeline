package ir.internship.logmonitor.ingester;
import ir.internship.logmonitor.model.LogComponent;
import ir.internship.logmonitor.model.LogEvent;
import ir.internship.logmonitor.model.LogLevel;
import java.time.LocalDateTime;

public class LogParser {
    public static LogEvent parse(String LogFileName, String Line) {
        String fileName = LogFileName;


        LogFileName = LogFileName.substring(0, LogFileName.lastIndexOf('.'));
        String fileTimestampSample = "YYYY-MM-DD_HH-MM-SS";
        int lenOfFileTimestamp = fileTimestampSample.length();
        String componentName = LogFileName.substring(0, LogFileName.length()-lenOfFileTimestamp+1);
        componentName = componentName.toUpperCase();
        LogComponent component; // how to = componentName; ?


        // Line format: 2021-07-12 01:22:42,114 [ThreadName] INFO package.name.ClassName - msg
        LocalDateTime timestamp;
        int year, month, day, hour, minute, second, nano;
        year = Integer.parseInt(Line.substring(0, Line.indexOf('-')));
        Line = Line.substring(Line.indexOf('-') + 1);

        month = Integer.parseInt(Line.substring(0, Line.indexOf('-')));
        Line = Line.substring(Line.indexOf('-') + 1);

        day = Integer.parseInt(Line.substring(0, Line.indexOf(' ')));
        Line = Line.substring(Line.indexOf(' ') + 1);

        hour = Integer.parseInt(Line.substring(0, Line.indexOf(':')));
        Line = Line.substring(Line.indexOf(':') + 1);

        minute = Integer.parseInt(Line.substring(0, Line.indexOf(':')));
        Line = Line.substring(Line.indexOf(':') + 1);

        second = Integer.parseInt(Line.substring(0, Line.indexOf(',')));
        Line = Line.substring(Line.indexOf(',') + 1);

        nano = Integer.parseInt(Line.substring(0, Line.indexOf(' '))) * 1000000;
        Line = Line.substring(Line.indexOf(' ') + 1);

        timestamp = LocalDateTime.of(year, month, day, hour, minute, second, nano);

        String threadName = Line.substring(0, Line.indexOf(' '));
        Line = Line.substring(Line.indexOf(' ') + 1);

        LogLevel level; // how to = Line.substring(0, Line.indexOf(' ')); ?
        Line = Line.substring(Line.indexOf(' ') + 1);

        String className = Line.substring(0, Line.indexOf(' '));
        Line = Line.substring(Line.indexOf(' '));
        Line = Line.substring(3);

        String message = Line;

        LogEvent Log = LogEvent(timestamp, component, level, threadName, className, message, fileName);

        return Log;
    }
}
