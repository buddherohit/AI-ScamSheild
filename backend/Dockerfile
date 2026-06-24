# ==============================================================================
# Stage 1: Build Phase
# ==============================================================================
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copy Maven settings and POM configuration
COPY pom.xml ./

# Pre-fetch dependency libraries
RUN mvn dependency:go-offline -B

# Copy project source codes
COPY src ./src

# Compile and package application (skip unit tests for immediate foundation setup speed)
RUN mvn package -DskipTests -B

# ==============================================================================
# Stage 2: Runtime Production Phase
# ==============================================================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy built JAR from Stage 1 compiler
COPY --from=builder /build/target/backend-1.0.0-SNAPSHOT.jar ./app.jar

# Expose backend REST API port
EXPOSE 8080

# Configure execution entrypoint
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
