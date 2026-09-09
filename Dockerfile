# Frontend: Angular SPA → src/main/resources/static
# Node 22 + npm-global pnpm: corepack+pnpm@11 on Node 20 fails with
# ERR_UNKNOWN_BUILTIN_MODULE during install on Render's builders.
FROM node:22-alpine AS frontend
WORKDIR /app

RUN npm install -g pnpm@11.8.0

COPY package.json pnpm-lock.yaml pnpm-workspace.yaml ./
COPY angular.json tsconfig.json tsconfig.app.json ./
COPY postcss.config.json ./
RUN pnpm install --frozen-lockfile

COPY src/main/web src/main/web
RUN pnpm run build

# Backend: Spring Boot jar (static assets already built)
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app

COPY gradle gradle
COPY gradlew build.gradle.kts settings.gradle.kts ./
RUN chmod +x gradlew \
  && ./gradlew dependencies --no-daemon || true

COPY src src
COPY --from=frontend /app/src/main/resources/static src/main/resources/static
RUN ./gradlew bootJar --no-daemon

# Runtime
FROM eclipse-temurin:25-jre-alpine

RUN addgroup -g 1001 -S appgroup \
  && adduser -u 1001 -S appuser -G appgroup

WORKDIR /app

COPY --from=build --chown=appuser:appgroup /app/build/libs/app.jar app.jar
RUN mkdir -p /app/data && chown appuser:appgroup /app/data

USER appuser
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
