FROM maven:3-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml ./
COPY src ./src
RUN mvn -B -DskipTests compile dependency:copy-dependencies -DincludeScope=runtime

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/classes ./target/classes
COPY --from=build /app/target/dependency ./target/dependency
COPY logs-backup ./logs-backup
EXPOSE 8080
ENTRYPOINT ["java", "-cp", "target/classes:target/dependency/*", "ir.logmonitor.Main"]
