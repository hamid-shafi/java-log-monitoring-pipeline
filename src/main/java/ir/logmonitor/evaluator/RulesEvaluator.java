package ir.logmonitor.evaluator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.logmonitor.database.Database;
import ir.logmonitor.model.LogAlert;
import ir.logmonitor.model.LogComponent;
import ir.logmonitor.model.LogEvent;
import ir.logmonitor.model.LogLevel;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.*;

public class RulesEvaluator {
    private final List<SingleLogRule> singleLogs = new ArrayList<>();
    private final List<FrequencyRule> frequencies = new ArrayList<>();
    private final List<RateRule> rates = new ArrayList<>();
    private final Database database;

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

    public RulesEvaluator() throws IOException, SQLException {
        database = new Database();
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

    public void process(LogEvent event) throws SQLException {
        for (SingleLogRule v : singleLogs) {
            LogAlert alert = v.process(event);
            if (alert != null) {
                database.insert(alert);
            }
        }
        for (FrequencyRule v : frequencies) {
            LogAlert alert = v.process(event);
            if (alert != null) {
                database.insert(alert);
            }
        }
        for (RateRule v : rates) {
            LogAlert alert = v.process(event);
            if (alert != null) {
                database.insert(alert);
            }
        }
    }

}