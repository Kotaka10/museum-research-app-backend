FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

COPY backend/pom.xml .
COPY backend/mvnw .
COPY backend/.mvn .mvn
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B


COPY backend/src src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

ENV SERVER_PORT=${PORT:-8080}

CMD ["java", "-jar", "app.jar", "--server.port=${PORT:-8080}"]