FROM sbtscala/scala-sbt:eclipse-temurin-alpine-25_36_1.11.6_3.7.3
WORKDIR /app
COPY . /app
RUN sbt -Denv=prod cli/assembly

FROM openjdk:25-jdk-slim
WORKDIR /app
COPY --from=0 /app/cli/target/scala-3.7.2/cli-app-assembly-0.1.0-SNAPSHOT.jar /app/cli-app.jar
ENTRYPOINT ["java", "-jar", "/app/cli-app.jar"]
