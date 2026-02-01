FROM maven:3.9.6-eclipse-temurin-21 AS builder
WORKDIR /build

COPY pom.xml .

COPY common/pom.xml common/
COPY common/grpc-common/pom.xml common/grpc-common/
COPY common/kafka-common/pom.xml common/kafka-common/
COPY booking-service/pom.xml booking-service/
COPY inventory-service/pom.xml inventory-service/
COPY order-service/pom.xml order-service/
COPY payment-service/pom.xml payment-service/
COPY trip-service/pom.xml trip-service/
COPY user-service/pom.xml user-service/
COPY apigateway/pom.xml apigateway/

RUN mvn dependency:go-offline -pl apigateway -am

COPY apigateway/src apigateway/src

RUN mvn package -pl apigateway -am -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /build/apigateway/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]