FROM adoptopenjdk/openjdk11:alpine-jre
ADD target/post-0.0.1-SNAPSHOT.jar post-service.jar
EXPOSE 8081
VOLUME /db
ENV UPLOAD-DIR=/db
ENTRYPOINT ["java", "-jar", "post-service.jar"]