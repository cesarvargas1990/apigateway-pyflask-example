FROM maven:3.8-jdk-11 AS build
WORKDIR /workspace
COPY src src/
COPY pom.xml .
COPY src/main/resources/apigateway_key.p12 apigateway_key.p12
RUN mvn clean package


FROM openjdk:11-jre-slim
COPY --from=build /workspace/target/api-gateway-0.0.1-SNAPSHOT.jar /app.jar
CMD ["java", "-jar", "/app.jar"]



ENTRYPOINT ["java","-jar","/app.jar"]