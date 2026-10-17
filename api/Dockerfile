# ---- Etapa 1: compilar ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B -DskipTests package

# ---- Etapa 2: ejecutar ----
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/target/autoparts-api-1.0.0.jar app.jar
ENV DATA_DIR=/data \
    IMAGES_DIR=/data/images
VOLUME /data
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
