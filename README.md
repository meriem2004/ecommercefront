# E-commerce Microservices Application

This is a microservices-based e-commerce application built with Spring Boot. The application consists of several microservices each handling a specific business domain.

## Architecture

- **User Service**: Manages user accounts and authentication
- **Product Service**: Handles product catalog and inventory
- **Cart Service**: Manages shopping carts
- **Order Service**: Processes orders
- **Payment Service**: Handles payment processing
- **Shipping Service**: Manages shipping and delivery
- **API Gateway**: Entry point for all client requests
- **Eureka Server**: Service discovery
- **Config Server**: Centralized configuration

## Prerequisites

- Java 17
- Maven
- Docker and Docker Compose

## Running the Application with Docker

1. **Build the project**:
   ```
   mvn clean package -DskipTests
   ```

2. **Start the application with Docker Compose**:
   ```
   docker-compose up -d
   ```

3. **Verify services are running**:
   ```
   docker-compose ps
   ```

4. **Access the API Gateway**:
   Open your browser and go to http://localhost:8080

5. **Access Eureka Dashboard**:
   Open your browser and go to http://localhost:8761

## Stopping the Application

To stop all containers:
```
docker-compose down
```

To stop and remove volumes (this will delete all data):
```
docker-compose down -v
```

## Running Locally (Without Docker)

To run the services locally without Docker:

1. Start PostgreSQL database
2. Set up the required databases:
   - user_db
   - product_db
   - cart_db
   - order_db
   - payment_db
   - shipping_db

3. Start the services in this order:
   - Eureka Server
   - Config Server
   - API Gateway
   - Remaining services

## Troubleshooting

Common issues:

1. **Database connection issues**: 
   - Ensure PostgreSQL is running and accessible
   - Check that database credentials are correct in application properties

2. **Service discovery issues**:
   - Verify Eureka Server is running
   - Check that services have registered with Eureka

3. **Config Server issues**:
   - Ensure Config Server is running before other services
   - Verify configuration files exist in config-repo directory 