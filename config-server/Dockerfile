# config-server/Dockerfile
FROM openjdk:17

WORKDIR /app

COPY target/config-server-0.0.1-SNAPSHOT.jar /app/config-server-0.0.1-SNAPSHOT.jar

RUN chmod 777 /app/config-server-0.0.1-SNAPSHOT.jar

# Default environment variables - will be overridden by docker-compose
ENV SPRING_PROFILES_ACTIVE=native \
    SPRING_CLOUD_CONFIG_SERVER_NATIVE_SEARCH_LOCATIONS=file:./config-repo

# Set JVM options for memory and garbage collection
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:+UseG1GC"

# Expose the Config Server port
EXPOSE 8888

# Healthcheck
HEALTHCHECK --interval=10s --timeout=5s --retries=3 \
    CMD curl -f http://config-server:8888/actuator/health || exit 1

# Run the application
CMD ["java", "-jar", "/app/config-server-0.0.1-SNAPSHOT.jar"]
