FROM amazoncorretto:17
VOLUME /tmp
ARG JAR_FILE=/build/libs/item-shelfer-1.0.0.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]