# Sử dụng JRE để chạy app (Nhẹ hơn JDK)
FROM eclipse-temurin:17-jre-alpine
RUN apk upgrade --no-cache
WORKDIR /app

# Copy file jar đã được build từ CI/CD runner vào image
# Việc này giúp giảm thời gian build Docker image đáng kể
COPY target/*.jar app.jar

# Mở port app
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
