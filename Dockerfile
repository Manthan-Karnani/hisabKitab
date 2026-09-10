# HISAB KITAB — Docker build for Render (repo root = this folder's parent)
# Build stage: compile Spring Boot + frontend (served from static/) into a jar
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY backend/pom.xml ./pom.xml
COPY backend/src ./src
RUN mvn -q package -DskipTests

# Run stage: slim JRE + the built jar
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/hisabkitab-1.0.0.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
