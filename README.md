# Log Monitoring and Alerting Pipeline

A Java application that monitors log files, streams log events through Apache Kafka, evaluates configurable alert rules, stores alerts in PostgreSQL, and exposes them through an HTTP API.

## How It Works

```text
Log Files → File Ingester → Kafka → Rule Evaluator → PostgreSQL → HTTP API
```

The file ingester processes existing and newly created log files. Log events are sent to Kafka and evaluated using rules defined in `rulesConfig.json`.

The project supports:

- Alerts for individual log levels
- Frequency-based alerts within a time window
- Overall log-rate alerts for each component
- Querying and filtering stored alerts through an HTTP API

## Tech Stack

- Java 21
- Apache Kafka
- PostgreSQL
- Maven
- Docker Compose
- JUnit 5

## Running Locally

Start Kafka and PostgreSQL:

```bash
docker compose up -d
```

Run the tests:

```bash
mvn test
```

Then run `ir.logmonitor.Main` from your IDE. The HTTP API will be available at:

```text
http://localhost:8080/alerts
```

## Docker and Liara

The Dockerfile builds the application with Java 21 and exposes HTTP port `8080`. On Liara, choose a Docker service with port `8080` and set these environment variables:

| Variable | Value |
| --- | --- |
| `DATABASE_URL` | PostgreSQL JDBC URL, such as `jdbc:postgresql://database-host:5432/logmonitor` |
| `DATABASE_USER` | PostgreSQL username |
| `DATABASE_PASSWORD` | PostgreSQL password |
| `KAFKA_BOOTSTRAP_SERVERS` | Reachable Kafka broker addresses, such as `broker-host:9092` |

Unset or blank values retain the existing local development defaults. PostgreSQL and Kafka must be reachable from the application container; `localhost` refers to that container. This Dockerfile runs only the Java application, so it does not start the services from Docker Compose. Kafka must advertise reachable broker addresses and provide the `logs` topic. The current Kafka client configuration uses plaintext connections without SASL authentication.

The HTTP endpoint `/` confirms that the API is running; it does not verify the complete Kafka processing pipeline.

## Project Background

This project was developed during my internship from an open-ended backend engineering assignment. It was my first substantial end-to-end Java application and my first experience using Git throughout a complete development lifecycle.

The main processing flow was specified, while implementation details such as the application structure, database schema, rule configuration format, alert evaluation logic, and API design were left open.
