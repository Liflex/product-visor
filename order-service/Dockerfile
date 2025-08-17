FROM eclipse-temurin:17-jre
ENV SPRING_PROFILES_ACTIVE=common,docker
WORKDIR /app
COPY order-service/target/order-service-1.0-SNAPSHOT.jar /app/app.jar
EXPOSE 9088 9098
ENTRYPOINT ["java","-jar","/app/app.jar"]

