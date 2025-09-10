# ビルドステージ
FROM adoptopenjdk:21-jdk-hotspot AS build
WORKDIR /app

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

COPY src src
RUN ./mvnw clean package -DskipTests

# 実行ステージ
FROM adoptopenjdk:21-jdk-hotspot
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

CMD ["/bin/sh","-c","/usr/local/openjdk-21/bin/java -jar app.jar"]