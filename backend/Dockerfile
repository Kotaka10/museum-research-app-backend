# ビルドステージ
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

COPY src src
RUN ./mvnw clean package -DskipTests

# 実行ステージ
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app

# ビルドステージから jar をコピー
COPY --from=build /app/target/*.jar app.jar

# Render の PORT 環境変数に対応
ENV SERVER_PORT=${PORT:-8080}

CMD ["java", "-jar", "app.jar", "--server.port=${PORT:-8080}"]