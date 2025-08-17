FROM eclipse-temurin:17-jre
ENV SPRING_PROFILES_ACTIVE=common,docker
WORKDIR /app
COPY authorization-service/target/authorization-service-1.0-SNAPSHOT.jar /app/app.jar
EXPOSE 9099
ENTRYPOINT ["java","-jar","/app/app.jar"]

