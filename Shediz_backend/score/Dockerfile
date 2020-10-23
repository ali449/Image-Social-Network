FROM adoptopenjdk/openjdk11:alpine-jre
ADD target/score-0.0.1-SNAPSHOT.jar score-service.jar
EXPOSE 8083
VOLUME /db
ENV SPRING_DATASOURCE_URL=jdbc:sqlite:/db/scores.db
ENTRYPOINT ["java", "-jar", "score-service.jar"]