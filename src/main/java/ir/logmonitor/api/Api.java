package ir.logmonitor.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import ir.logmonitor.database.Database;
import ir.logmonitor.model.LogAlert;
import ir.logmonitor.model.LogComponent;
import ir.logmonitor.model.LogLevel;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class Api {
    private final Database database;
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT);

    public Api() throws SQLException {
        database = new Database();
    }

    private String buildWhere(String query) {

        if (query == null || query.isBlank()) {
            return "";
        }

        String ruleName = null;
        LogComponent component = null;
        LogLevel level = null;
        Boolean levelMustBeNull = null;
        LocalDateTime from = null;
        LocalDateTime to = null;

        String[] params = query.split("&");
        for (String param : params) {
            String[] kv = param.split("=", 2);

            if (kv.length != 2)
                continue; // err
            String key = kv[0];
            String value = kv[1];

            switch (key) {
                case "ruleName":
                    ruleName = value;
                    break;
                case "component":
                    component = LogComponent.valueOf(value);
                    break;
                case "level":
                    if (value.equalsIgnoreCase("null"))
                        levelMustBeNull = true;
                    else
                        level = LogLevel.valueOf(value);
                    break;
                case "from":
                    from = LocalDateTime.parse(value);
                    break;
                case "to":
                    to = LocalDateTime.parse(value);
                    break;
            }
        }

        return Database.buildWhere(
                ruleName,
                component,
                level,
                levelMustBeNull,
                from,
                to
        );
    }

    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/", new HttpHandler() {
            public void handle(HttpExchange exchange) throws IOException {

                String response = "Log Monitor API is running.";

                exchange.sendResponseHeaders(200, response.length());

                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }
        });

        server.createContext("/alerts", new HttpHandler() {
            public void handle(HttpExchange exchange) throws IOException {
                try {
                    System.out.println("GET /alerts");
                    String method = exchange.getRequestMethod();

                    if (method.equals("GET")) {
                        List<LogAlert> alerts = database.getAlerts(
                                buildWhere(exchange.getRequestURI().getQuery())
                        );

                        String response = mapper.writeValueAsString(alerts);

                        exchange.getResponseHeaders().set("Content-Type", "application/json");

                        exchange.sendResponseHeaders(200, response.length());

                        OutputStream os = exchange.getResponseBody();
                        os.write(response.getBytes());
                        os.close();
                    } else if (method.equals("DELETE")) {
                        String query = exchange.getRequestURI().getQuery();

                        if (query == null || query.isBlank()) {
                            database.deleteAlerts();
                        }
                        else if (!query.contains("=")) {
                            database.deleteAlert(Integer.parseInt(query));
                        }
                        else {
                            database.deleteAlerts(buildWhere(query));
                        }

                        exchange.sendResponseHeaders(204, -1);
                    } else {
                        exchange.sendResponseHeaders(405, -1);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    String response = "Internal Server Error";

                    exchange.sendResponseHeaders(
                            500,
                            response.length()
                    );

                    OutputStream os = exchange.getResponseBody();
                    os.write(response.getBytes());
                    os.close();
                }
            }
        });

        server.start();
        System.out.println("API started on port 8080");
    }

    public void close() throws Exception {
        database.close();
    }
}
