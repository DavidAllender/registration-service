FROM openjdk:8-jdk-alpine
ADD target/registration-service-0.0.1-SNAPSHOT.jar registration-service.jar
EXPOSE 8080 5005
ENTRYPOINT ["java", "-jar","registration-service.jar"]