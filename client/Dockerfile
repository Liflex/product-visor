FROM eclipse-temurin:17-jre
ENV SPRING_PROFILES_ACTIVE=common,docker
WORKDIR /app
COPY client/target/client-1.0-SNAPSHOT.jar /app/app.jar
EXPOSE 9089 9100
ENTRYPOINT ["java","-jar","/app/app.jar"]

