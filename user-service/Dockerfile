# user-service/Dockerfile
FROM openjdk:17

WORKDIR /app

COPY target/user-service-0.0.1-SNAPSHOT.jar /app/user-service-0.0.1-SNAPSHOT.jar

RUN chmod 777 /app/user-service-0.0.1-SNAPSHOT.jar

# Default environment variables - will be overridden by docker-compose
ENV SPRING_PROFILES_ACTIVE=dev

# Set JVM options for memory and garbage collection
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

# Expose the Cart Service port
EXPOSE 8086

# Healthcheck
HEALTHCHECK --interval=10s --timeout=5s --retries=3 \
    CMD curl -f http://user-service:8086/actuator/health || exit 1

# Run the application
CMD ["java", "-jar", "/app/user-service-0.0.1-SNAPSHOT.jar"]