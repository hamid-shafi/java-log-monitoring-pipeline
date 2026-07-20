package ir.internship.logmonitor.evaluator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.internship.logmonitor.model.LogEvent;
import ir.internship.logmonitor.model.LogLevel;

import java.io.IOException;
import java.io.InputStream;

public class RulesEvaluator {

    private boolean singleLogEnabled;
    private LogLevel singleLogLevel;

    private void singleLogInit(JsonNode v) {
        singleLogEnabled = v.get("enabled").asBoolean();
        singleLogLevel = LogLevel.valueOf(v.get("level").asText());
    }

    private boolean frequencyEnabled;
    private LogLevel frequencyLevel;
    private int frequencyWindowSeconds;
    private int frequencyThreshold;

    private void frequencyInit(JsonNode v) {
        frequencyEnabled = v.get("enabled").asBoolean();
        frequencyLevel = LogLevel.valueOf(v.get("level").asText());
        frequencyWindowSeconds = v.get("windowSeconds").asInt();
        frequencyThreshold = v.get("threshold").asInt();
    }

    private boolean rateEnabled;
    private int rateWindowSeconds;
    private int rateThreshold;

    private void rateInit(JsonNode v) {
        rateEnabled = v.get("enabled").asBoolean();
        rateWindowSeconds = v.get("windowSeconds").asInt();
        rateThreshold = v.get("threshold").asInt();
    }


    public RulesEvaluator() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        InputStream config = getClass().getClassLoader().getResourceAsStream("rulesConfig.json");
        JsonNode root = mapper.readTree(config);
        singleLogInit(root.get("singleLog"));
        frequencyInit(root.get("frequency"));
        rateInit(root.get("rate"));
    }


    public void process(LogEvent event) {

    }

    private void evaluateSingleLogRule(LogEvent event) {

    }

    private void evaluateFrequencyRule(LogEvent event) {

    }

    private void evaluateRateRule(LogEvent event) {

    }

    private void createAlert() {

    }

}