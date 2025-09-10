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

# Render/Railway が割り当てる PORT 環境変数を使う
EXPOSE 8080
CMD java -jar app.jar --server.port=$PORT