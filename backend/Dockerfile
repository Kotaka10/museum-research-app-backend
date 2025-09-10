FROM ubuntu:22.04 AS build
ARG CACHE_BREAK=1
WORKDIR /app

# JDK インストール
RUN apt-get update && apt-get install -y openjdk-21-jdk maven && rm -rf /var/lib/apt/lists/*

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

COPY src src
RUN ./mvnw clean package -DskipTests

FROM ubuntu:22.04
WORKDIR /app

# JDK をインストール
RUN apt-get update && apt-get install -y openjdk-21-jdk && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/*.jar app.jar

CMD ["sh", "-c", "java -jar app.jar"]