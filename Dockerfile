FROM sbtscala/scala-sbt:eclipse-temurin-alpine-24.0.1_9_1.12.9_3.8.3
WORKDIR /app
COPY . /app
RUN sbt -Denv=prod cli/assembly

FROM eclipse-temurin:24-jre-alpine
WORKDIR /app
COPY --from=0 /app/cli/target/scala-3.8.3/cli-app-assembly-0.1.0-SNAPSHOT.jar /app/cli-app.jar
ENTRYPOINT ["java", "-jar", "/app/cli-app.jar"]
