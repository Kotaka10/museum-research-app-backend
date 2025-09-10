FROM adoptopenjdk:21-jdk-hotspot
WORKDIR /app

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

COPY src src
RUN ./mvnw clean package -DskipTests

COPY target/*.jar app.jar
CMD ["sh", "-c", "java -jar app.jar --server.port=$PORT"]
