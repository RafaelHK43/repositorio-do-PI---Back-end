# Build stage
FROM eclipse-temurin:24-jdk-alpine AS build

WORKDIR /app

COPY . .

RUN chmod +x ./mvnw && \
    ./mvnw clean install -DskipTests

# Runtime stage
FROM eclipse-temurin:24-jre-alpine

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
