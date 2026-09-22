FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src src
RUN mvn -q package

FROM eclipse-temurin:25-jre
RUN useradd --system --uid 10001 appuser
WORKDIR /app
COPY --from=build /workspace/target/backend-0.0.1-SNAPSHOT.jar app.jar
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
