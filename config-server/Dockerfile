# Use OpenJDK as base image
FROM openjdk:17

# Set working directory inside container
WORKDIR /app

# Copy the built JAR file into the container
COPY target/config-server-0.0.1-SNAPSHOT.jar /app/config-server-0.0.1-SNAPSHOT.jar

# Give the container access to the JAR file
RUN chmod 777 /app/config-server-0.0.1-SNAPSHOT.jar

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=native \
    SPRING_CLOUD_CONFIG_SERVER_NATIVE_SEARCH_LOCATIONS=file:./config-repo \
    EUREKA_CLIENT_ENABLED=false

# Expose the Config Server port
EXPOSE 8888

# Healthcheck (optional)
HEALTHCHECK --interval=10s --timeout=5s --retries=3 \
    CMD curl -f http://localhost:8888/actuator/health || exit 1

# Run the application
CMD ["java", "-jar", "/app/config-server-0.0.1-SNAPSHOT.jar"]
