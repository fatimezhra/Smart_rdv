# Étape 1 : Build Frontend React
FROM node:18 AS frontend-build
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm install
COPY frontend/ .
RUN CI=false npm run build
# Étape 2 : Build Backend Spring Boot
FROM maven:3.9-eclipse-temurin-21 AS backend-build
WORKDIR /app
COPY . .
COPY --from=frontend-build /app/frontend/build src/main/resources/static
RUN mvn clean package -DskipTests

# Étape 3 : Run
FROM container-registry.oracle.com/java/openjdk:25
WORKDIR /app
COPY --from=backend-build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
