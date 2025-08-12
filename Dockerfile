# build stage
FROM eclipse-temurin:20-jdk AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# run stage
FROM eclipse-temurin:20-jre
WORKDIR /app
COPY --from=build /app/target/notafacil-backend-1.0.0.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
