FROM adoptopenjdk/openjdk11:alpine-jre
ADD target/eureka-0.0.1-SNAPSHOT.jar discovery-service.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "discovery-service.jar"]