FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml checkstyle.xml ./
COPY src ./src
RUN mvn -DskipTests clean package

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/EncrptionProgram-1.0-SNAPSHOT.jar /app/app.jar

ENV PORT=10000
EXPOSE 10000

CMD ["sh", "-c", "java -jar /app/app.jar"]
