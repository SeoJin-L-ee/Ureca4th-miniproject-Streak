# ---- Stage 1: 프론트엔드 빌드 (vite build -> src/main/resources/static) ----
FROM node:22-alpine AS frontend-build
WORKDIR /workspace/frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# ---- Stage 2: 백엔드 빌드 (bootJar) ----
FROM eclipse-temurin:21-jdk AS backend-build
WORKDIR /workspace
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
RUN chmod +x gradlew
COPY src ./src
COPY --from=frontend-build /workspace/src/main/resources/static ./src/main/resources/static
RUN ./gradlew bootJar -x test --no-daemon

# ---- Stage 3: 실행 이미지 ----
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=backend-build /workspace/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
