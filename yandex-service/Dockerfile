FROM openjdk:17-jdk-slim

WORKDIR /app

# Копируем JAR файл
COPY target/yandex-service-1.0-SNAPSHOT.jar app.jar

# Открываем порт
EXPOSE 9091

# Запускаем приложение
ENTRYPOINT ["java", "-jar", "app.jar"]
