# Use OpenJDK as base image
FROM openjdk:17

# Set working directory inside container
WORKDIR /app

# Copy the built JAR file into the container
COPY target/product-service-0.0.1-SNAPSHOT.jar /app/product-service-0.0.1-SNAPSHOT.jar

# Give the container access to the JAR file
RUN chmod 777 /app/product-service-0.0.1-SNAPSHOT.jar

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=dev \
    SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-product:5432/product_db \
    SPRING_DATASOURCE_USERNAME=postgres \
    SPRING_DATASOURCE_PASSWORD=postgres \
    SPRING_JPA_HIBERNATE_DDL_AUTO=update \
    SPRING_JPA_SHOW_SQL=true \
    EUREKA_CLIENT_ENABLED=false \
    SPRING_CLOUD_CONFIG_ENABLED=false

# Expose the Product Service port
EXPOSE 8081

# Healthcheck (optional)
HEALTHCHECK --interval=10s --timeout=5s --retries=3 \
    CMD curl -f http://localhost:8081/actuator/health || exit 1

# Run the application
CMD ["java", "-jar", "/app/product-service-0.0.1-SNAPSHOT.jar"]
