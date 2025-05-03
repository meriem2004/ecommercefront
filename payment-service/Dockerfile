# payment-service/Dockerfile
FROM openjdk:17

WORKDIR /app

COPY target/payment-service-0.0.1-SNAPSHOT.jar /app/payment-service-0.0.1-SNAPSHOT.jar

RUN chmod 777 /app/payment-service-0.0.1-SNAPSHOT.jar

# Default environment variables - will be overridden by docker-compose
ENV SPRING_PROFILES_ACTIVE=dev

# Set JVM options for memory and garbage collection
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

# Expose the Payment Service port
EXPOSE 8084

# Healthcheck
HEALTHCHECK --interval=10s --timeout=5s --retries=3 \
    CMD curl -f http://payment-service:8084/actuator/health || exit 1

# Run the application
CMD ["java", "-jar", "/app/payment-service-0.0.1-SNAPSHOT.jar"]