# Build stage (uses Gradle image so gradlew is not required)
FROM gradle:8-jdk21-alpine AS build
WORKDIR /app

COPY build.gradle .
COPY settings.gradle .
COPY gradle.properties .
COPY gradle gradle
COPY src src

RUN gradle build -x test --no-daemon || true
RUN gradle bootJar --no-daemon

# Run stage
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S spring && adduser -S spring -G spring
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
USER spring
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
