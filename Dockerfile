FROM maven:3.8.1-openjdk-17-slim AS build

COPY . /app
WORKDIR /app

RUN mvn -f /app/pom.xml clean package

RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../slideshow-1.0.0.jar)

FROM openjdk:17-jdk-slim

COPY --from=build /app/target/dependency/ /app

EXPOSE 8080

ENTRYPOINT ["java", "-cp", "/app", "-Dconfig=/app/config.yaml", "org.iish.slideshow.Application"]
