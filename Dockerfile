# syntax=docker/dockerfile:1

# ---- Build ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Dependências primeiro: só baixa de novo quando o pom.xml muda.
# Sem "--mount=type=cache": o Railway exige um formato próprio para cache mounts.
COPY pom.xml .
RUN mvn -B -q dependency:go-offline

COPY src ./src
# Os testes precisam de MySQL; rode-os com o serviço api-test do docker-compose
RUN mvn -B -q package -DskipTests

# ---- Runtime ----
FROM eclipse-temurin:25-jre-alpine
RUN addgroup -S app && adduser -S app -G app
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

USER app
ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-XX:MaxRAMPercentage=75"
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
