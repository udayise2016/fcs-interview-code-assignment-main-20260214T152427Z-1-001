# Multi-stage build for Warehouse Management System
FROM eclipse-temurin:17-jdk-alpine AS builder

ARG MAVEN_VERSION=3.9.4
ARG MAVEN_OPTS="-Xmx1024m"

# Install Maven
RUN apk add --no-cache curl tar bash \
    && curl -sL https://archive.apache.org/dist/maven/maven-${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz | tar -xz -C /tmp \
    && mv /tmp/apache-maven-${MAVEN_VERSION} /opt/maven \
    && ln -sf /opt/maven/bin/mvn /usr/bin/mvn

# Set working directory
WORKDIR /app

# Copy pom.xml first for better Docker layer caching
COPY pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:17-jre-alpine

# Install required runtime dependencies
RUN apk add --no-cache curl

# Create app user
RUN addgroup -g 1001 appgroup && adduser -u 1001 -G appgroup -s /bin/sh -D appuser

# Set working directory
WORKDIR /app

# Copy the built application
COPY --from=builder /app/target/quarkus-app/quarkus-run.jar ./quarkus-run.jar
COPY --from=builder /app/target/quarkus-app/lib ./lib
COPY --from=builder /app/target/quarkus-app/app ./app

# Change ownership
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/q/health/live || exit 1

# Expose port
EXPOSE 8080

# JVM options
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseG1GC -XX:+UseStringDeduplication"

# Start the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar quarkus-run.jar"]
