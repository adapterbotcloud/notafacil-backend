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
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=10s --start-period=300s --retries=6 CMD bash -c 'echo > /dev/tcp/localhost/8080' 2>/dev/null || exit 1
ENTRYPOINT ["java", "-jar", "app.jar"]