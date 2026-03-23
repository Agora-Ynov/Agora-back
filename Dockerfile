FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Utilisateur non-root pour limiter la surface d'attaque
RUN addgroup -S agora && adduser -S agora -G agora

COPY target/*.jar app.jar
RUN chown agora:agora app.jar

USER agora

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
