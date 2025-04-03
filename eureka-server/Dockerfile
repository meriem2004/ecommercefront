# Use an OpenJDK base image
FROM openjdk:17
  
  # Set the working directory inside the container
WORKDIR /app
  
  # Copy the built JAR file into the container
COPY target/eureka-server-0.0.1-SNAPSHOT.jar /app/eureka-server-0.0.1-SNAPSHOT.jar

  # Give the container access to the JAR file
RUN chmod 777 /app/eureka-server-0.0.1-SNAPSHOT.jar
  
  # Set environment variables
ENV SPRING_PROFILES_ACTIVE=dev \
EUREKA_CLIENT_REGISTER_WITH_EUREKA=false \
EUREKA_CLIENT_FETCH_REGISTRY=false
  
  # Expose the Eureka server port
EXPOSE 8761
  
  # Healthcheck (optional)
HEALTHCHECK --interval=10s --timeout=5s --retries=3 \
CMD curl -f http://localhost:8761/actuator/health || exit 1
  
  # Run the application
CMD ["java", "-jar", "/app/eureka-server-0.0.1-SNAPSHOT.jar"]
