# --- Build Stage ---
FROM maven:3.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
# Copy pom.xml first to cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B
# Copy source and build JAR
COPY src ./src
RUN mvn clean package -DskipTests

# --- Run Stage ---
FROM eclipse-temurin:17-jre-alpine
RUN apk upgrade --no-cache && \
    apk add --no-cache curl
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
  CMD curl -f http://localhost:8080/api-docs || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
