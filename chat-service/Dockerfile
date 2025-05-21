FROM eclipse-temurin:21-jdk-alpine

VOLUME /tmp
ARG JAR_FILE=target/chat-service-0.0.1-SNAPSHOT.jar

COPY ${JAR_FILE} app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "/app.jar"]
