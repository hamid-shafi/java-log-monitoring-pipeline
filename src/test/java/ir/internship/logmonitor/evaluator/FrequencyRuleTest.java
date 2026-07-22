package ir.internship.logmonitor.evaluator;

import ir.internship.logmonitor.model.LogAlert;
import ir.internship.logmonitor.model.LogComponent;
import ir.internship.logmonitor.model.LogEvent;
import ir.internship.logmonitor.model.LogLevel;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class FrequencyRuleTest {

    @Test
    void shouldCreateAlertWhenThresholdReached() {
        FrequencyRule rule = new FrequencyRule(
                "too_many_errors",
                LogLevel.ERROR,
                10,
                3
        );

        LocalDateTime time = LocalDateTime.now();

        LogEvent e1 = new LogEvent(
                time,
                LogComponent.AUTH,
                LogLevel.ERROR,
                "thread",
                "LoginService",
                "error1"
        );

        LogEvent e2 = new LogEvent(
                time.plusSeconds(1),
                LogComponent.AUTH,
                LogLevel.ERROR,
                "thread",
                "LoginService",
                "error2"
        );

        LogEvent e3 = new LogEvent(
                time.plusSeconds(2),
                LogComponent.AUTH,
                LogLevel.ERROR,
                "thread",
                "LoginService",
                "error3"
        );

        assertNull(rule.process(e1));
        assertNull(rule.process(e2));
        LogAlert alert = rule.process(e3);

        assertNotNull(alert);

        assertEquals(
                "too_many_errors",
                alert.ruleName
        );
        assertEquals(
                "AUTH",
                alert.component
        );
        assertEquals(
                "ERROR",
                alert.level
        );
        assertTrue(alert.description.contains("error2"));
        assertTrue(alert.description.contains("error3"));
    }
}
