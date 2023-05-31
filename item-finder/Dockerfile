FROM --platform=linux/arm64 azul/zulu-openjdk-alpine:17 as builder
ARG JAR_FILE=/build/libs/item-finder-1.0.0.jar
COPY ${JAR_FILE} application.jar
RUN java -Djarmode=layertools -jar application.jar extract

FROM azul/zulu-openjdk-alpine:17
ENV SPRING_PROFILES_ACTIVE docker
COPY --from=builder dependencies/ ./
COPY --from=builder snapshot-dependencies/ ./
COPY --from=builder spring-boot-loader/ ./
COPY --from=builder application/ ./
ENTRYPOINT ["java", "org.springframework.boot.loader.JarLauncher", "-XX:MaxRAMPercentage=75", "-XX:+UseG1GC"]