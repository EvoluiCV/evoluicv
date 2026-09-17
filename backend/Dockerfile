# syntax=docker/dockerfile:1.7

# ---- builder ----
FROM eclipse-temurin:25-jdk@sha256:010ab2b79329b951bf052dbf515843ab345ae4e4f0ef72287d85c9fa54da275f AS builder
WORKDIR /app
ARG CACHE_SCOPE=local

# Copia wrapper e pom primeiro para cachear dependências
COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN --mount=type=cache,id=evoluicv-m2-${CACHE_SCOPE},target=/root/.m2,sharing=locked \
    chmod +x mvnw && ./mvnw -B -q dependency:go-offline

# Build
COPY src ./src
RUN --mount=type=cache,id=evoluicv-m2-${CACHE_SCOPE},target=/root/.m2,sharing=locked \
    ./mvnw -B -q -DskipTests package

# ---- runner ----
FROM eclipse-temurin:25-jre@sha256:611e326900ba9edcedfb246ede24efc66a911fa496ac623db9f33cae88a2d83b AS runner
WORKDIR /app

RUN groupadd --system spring \
  && useradd --system --gid spring --home /app --shell /sbin/nologin spring

COPY --from=builder /app/target/*.jar /app/app.jar

USER spring:spring
EXPOSE 8080

ENV JAVA_OPTS=""
ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -jar /app/app.jar"]
