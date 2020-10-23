FROM adoptopenjdk/openjdk11:alpine-jre
ADD target/gateway-0.0.1-SNAPSHOT.jar user-service.jar
EXPOSE 8080
VOLUME /db
ENV SPRING_DATASOURCE_URL=jdbc:sqlite:/db/users.db
ENV UPLOAD-DIR=/db
ENTRYPOINT ["java", "-jar", "user-service.jar"]

# add database folder to shared files from docker desktop setting
# docker run --name users -it -p 8080:8080 -v c:/Users/Moradian/IdeaProjects/Shediz/gateway/src/main/db:/db user-service