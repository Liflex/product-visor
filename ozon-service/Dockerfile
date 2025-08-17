FROM eclipse-temurin:17-jre
ENV SPRING_PROFILES_ACTIVE=common,docker
WORKDIR /app
COPY ozon-service/target/ozon-service-1.0-SNAPSHOT.jar /app/app.jar
EXPOSE 9097
ENTRYPOINT ["java","-jar","/app/app.jar"]

