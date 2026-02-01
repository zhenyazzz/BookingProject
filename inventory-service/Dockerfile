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

RUN mvn dependency:go-offline -pl inventory-service -am

COPY inventory-service/src inventory-service/src
COPY common/grpc-common/src common/grpc-common/src
COPY common/kafka-common/src common/kafka-common/src

RUN mvn package -pl inventory-service -am -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /build/inventory-service/target/*.jar app.jar

EXPOSE 8082
EXPOSE 9090

ENTRYPOINT ["java", "-jar", "app.jar"]