FROM eclipse-temurin:21-jdk-alpine

VOLUME /tmp
ARG JAR_FILE=target/config-service-0.0.1-SNAPSHOT.jar

COPY ${JAR_FILE} app.jar
COPY src/main/resources/config-repo ./config-repo

EXPOSE 8888
ENTRYPOINT ["java", "-jar", "/app.jar", "--spring.profiles.active=docker,native"]