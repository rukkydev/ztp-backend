# ==========================================
# Stage 1: Build Stage (Maven + JDK 25)
# ==========================================
FROM eclipse-temurin:25-jdk-noble AS build

WORKDIR /build

# Copy Maven wrapper files and pom.xml first to optimize layer caching
COPY pom.xml ./
COPY .mvn/ .mvn/
COPY mvnw ./

# Ensure maven wrapper script has execute permissions and fetch dependencies
RUN chmod +x ./mvnw && ./mvnw dependency:go-offline -B || true

# Copy source code and build the production artifact
COPY src/ src/
RUN ./mvnw clean package -DskipTests -B

# ==========================================
# Stage 2: Runtime Stage (JRE 25)
# ==========================================
FROM eclipse-temurin:25-jre-noble AS runtime

WORKDIR /app

# Create a non-root system user for security best practices
RUN groupadd -r ztpuser && useradd -r -g ztpuser ztpuser

# Copy the built jar from the build stage as app.jar
COPY --from=build /build/target/ztp-0.0.1-SNAPSHOT.jar app.jar

# Ensure upload/working directories exist and set ownership
RUN mkdir -p /app/uploads/avatars /app/uploads/reports && chown -R ztpuser:ztpuser /app

USER ztpuser:ztpuser

# Fallback port 10000 for Render / production
ENV PORT=10000
EXPOSE 10000

# Execute Spring Boot passing the Render PORT environment variable
ENTRYPOINT ["sh", "-c", "exec java -Dserver.port=${PORT:-10000} -jar app.jar"]
