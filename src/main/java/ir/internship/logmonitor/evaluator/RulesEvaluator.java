package ir.internship.logmonitor.evaluator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.internship.logmonitor.model.LogAlert;
import ir.internship.logmonitor.model.LogComponent;
import ir.internship.logmonitor.model.LogEvent;
import ir.internship.logmonitor.model.LogLevel;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class RulesEvaluator {
    private final List<SingleLogRule> singleLogs = new ArrayList<>();
    private final List<FrequencyRule> frequencies = new ArrayList<>();
    private final List<RateRule> rates = new ArrayList<>();

    private void addSingleLogRule(JsonNode v) {
        singleLogs.add(new SingleLogRule(
                v.get("ruleName").asText(),
                LogLevel.valueOf(v.get("level").asText())
        ));
    }

    private void addFrequencyRule(JsonNode v) {
        frequencies.add(new FrequencyRule(
                v.get("ruleName").asText(),
                LogLevel.valueOf(v.get("level").asText()),
                v.get("windowSeconds").asInt(),
                v.get("threshold").asInt()
        ));
    }

    private void addRateRule(JsonNode v) {
        rates.add(new RateRule(
                v.get("ruleName").asText(),
                v.get("windowSeconds").asInt(),
                v.get("threshold").asInt()
        ));
    }

    public RulesEvaluator() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InputStream config = getClass().getClassLoader().getResourceAsStream("rulesConfig.json");
        JsonNode root = mapper.readTree(config);
        for (JsonNode v : root) {
            String type = v.get("type").asText();
            if (type.equals("singleLog")) {
                addSingleLogRule(v);
            }
            else if (type.equals("frequency")) {
                addFrequencyRule(v);
            }
            else if (type.equals("rate")) {
                addRateRule(v);
            }
        }
    }

    public void process(LogEvent event) {
        for (SingleLogRule v : singleLogs) {
            LogAlert alert = v.process(event);
            if (alert != null) {
                sendToSQL(alert);
            }
        }
        for (FrequencyRule v : frequencies) {
            LogAlert alert = v.process(event);
            if (alert != null) {
                sendToSQL(alert);
            }
        }
        for (RateRule v : rates) {
            LogAlert alert = v.process(event);
            if (alert != null) {
                sendToSQL(alert);
            }
        }
    }

    private void sendToSQL(LogAlert a) {
        // send data to Postgre SQL
    }

}