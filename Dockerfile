# 1. Aşama: Gradle ile projeyi derle ve .jar paketini oluştur
FROM gradle:8.10-jdk17 AS build
WORKDIR /app
COPY . .
RUN chmod +x gradlew && ./gradlew bootJar --no-daemon

# 2. Aşama: Sadece hafif Java 17 JRE ile uygulamayı çalıştır
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]