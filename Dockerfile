# Utilisation de l'image Oracle pour Java 25
FROM container-registry.oracle.com/java/openjdk:25

# Dossier de travail
WORKDIR /app

# Copie du JAR
COPY target/ProjectGL-0.0.1-SNAPSHOT.jar app.jar

# Port de ton application
EXPOSE 8081

# Lancement
ENTRYPOINT ["java", "-jar", "app.jar"]