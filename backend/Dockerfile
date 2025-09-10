# Java 21 の公式イメージ
FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app

# Maven Wrapper と設定をコピー
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline -B

# ソースコードをコピー
COPY src src
RUN ./mvnw clean package -DskipTests

# ビルド成果物をコピー
COPY target/*.jar app.jar

# Render の PORT 環境変数に対応
ENV SERVER_PORT=${PORT:-8080}

# アプリ起動
CMD ["java", "-jar", "app.jar", "--server.port=${PORT:-8080}"]