FROM eclipse-temurin:17-jre
ENV SPRING_PROFILES_ACTIVE=common,docker
WORKDIR /app
COPY product-visor-backend/target/product-visor-backend-1.0-SNAPSHOT.jar /app/app.jar
EXPOSE 8085 9093
VOLUME [ "/data/images" ]
ENTRYPOINT ["java","-jar","/app/app.jar"]

