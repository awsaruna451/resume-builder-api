# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM --platform=linux/amd64 maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

# Cache dependencies layer separately — only re-downloads when pom.xml changes
COPY pom.xml .
RUN mvn dependency:go-offline -B -q

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests -q

# ── Stage 2: Runtime ───────────────────────────────────────────────────────────
FROM --platform=linux/amd64 eclipse-temurin:21-jre-alpine AS runner

WORKDIR /app

# Non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Upload directory
#RUN mkdir -p /app/uploads && chown spring:spring /app/uploads
#VOLUME ["/app/uploads"]

# Copy the fat JAR from builder — path matches WORKDIR /app + Maven output
COPY --from=builder /app/target/resume-builder-api-1.0.0.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]