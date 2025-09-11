# ビルドステージ
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Maven 依存関係ダウンロード
COPY backend/pom.xml .
COPY backend/mvnw .
COPY backend/.mvn .mvn
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

# ソースコードをコピーしてビルド
COPY backend/src src
RUN ./mvnw clean package -DskipTests

# 実行ステージ
FROM eclipse-temurin:21-jdk-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Render の PORT 環境変数に対応
ENV SERVER_PORT=${PORT:-8080}

# Spring Boot アプリを起動
CMD ["java", "-jar", "app.jar", "--server.port=${PORT:-8080}"]