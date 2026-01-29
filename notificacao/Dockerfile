FROM maven:3.8.7-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jdk

COPY --from=build /app/target/notificacao-0.0.1-SNAPSHOT.jar notificacao.jar

ENTRYPOINT ["java", "-jar", "/notificacao.jar"]
