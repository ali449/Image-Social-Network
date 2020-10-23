FROM adoptopenjdk/openjdk11:alpine-jre
ADD target/recommender-0.0.1-SNAPSHOT.jar recommender-service.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "recommender-service.jar"]