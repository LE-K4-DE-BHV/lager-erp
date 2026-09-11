# Stage 1: Build
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q

# Stage 2: Runtime
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
RUN addgroup -S lager && adduser -S lager -G lager
COPY --from=build /app/target/*.jar app.jar
RUN chown lager:lager app.jar
USER lager
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
