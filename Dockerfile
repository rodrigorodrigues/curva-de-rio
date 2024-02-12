FROM openjdk:21-jdk-slim as BUILD

WORKDIR /app

COPY src /app/src
COPY pom.xml /app
COPY .mvn /app/.mvn
COPY mvnw /app/mvnw

RUN ./mvnw clean package -DskipTests

FROM openjdk:21-jdk-slim as RUNTIME

COPY --from=BUILD /app/target/curva-de-rio-0.0.1-SNAPSHOT.jar /rinha.jar

EXPOSE 8080

ENTRYPOINT [ "java","-jar", "./rinha.jar" ]
