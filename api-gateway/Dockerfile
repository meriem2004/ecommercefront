# Use OpenJDK as base image
FROM openjdk:17

# Set working directory inside container
WORKDIR /app

# Copy the built JAR file into the container
COPY target/api-gateway-0.0.1-SNAPSHOT.jar /app/api-gateway-0.0.1-SNAPSHOT.jar

# Give the container access to the JAR file
RUN chmod 777 /app/api-gateway-0.0.1-SNAPSHOT.jar

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=dev \
    EUREKA_CLIENT_ENABLED=false \
    SPRING_CLOUD_CONFIG_ENABLED=false

# Expose the API Gateway port
EXPOSE 8080

# Healthcheck (optional)
HEALTHCHECK --interval=10s --timeout=5s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
CMD ["java", "-jar", "/app/api-gateway-0.0.1-SNAPSHOT.jar"]
