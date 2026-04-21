# build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# run stage
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/notafacil-1.0.0.jar app.jar
RUN apt-get update && apt-get install -y curl && apt-get clean && rm -rf /var/lib/apt/lists/
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 CMD curl -f http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]