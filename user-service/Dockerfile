FROM maven:3.9.8-eclipse-temurin-21 AS builder
WORKDIR /workspace

COPY user-service/ ./user-service/
RUN cd user-service \
    && mvn -q clean package -DskipTests


FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /workspace/user-service/target/user-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8082

ENTRYPOINT ["java","-jar","app.jar"]
