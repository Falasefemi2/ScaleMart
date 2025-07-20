# ScaleMart

ScaleMart is a microservices-based e-commerce application built with Spring Boot. It consists of three main services: a Eureka Server for service discovery, a User Service for managing users and authentication, and a Product Service for managing products.

## Table of Contents

- [Project Description](#project-description)
- [Modules](#modules)
  - [Eureka Server](#eureka-server)
  - [User Service](#user-service)
  - [Product Service](#product-service)
- [Technologies Used](#technologies-used)
- [Prerequisites](#prerequisites)
- [Getting Started](#getting-started)
- [API Endpoints](#api-endpoints)
  - [User Service Endpoints](#user-service-endpoints)
  - [Product Service Endpoints](#product-service-endpoints)
- [Configuration](#configuration)
- [Database](#database)
- [Security](#security)

## Project Description

ScaleMart is designed to be a scalable and resilient e-commerce platform. By leveraging a microservices architecture, each core functionality (user management, product management, service discovery) is encapsulated in its own independent service. This modular approach allows for easier development, deployment, and maintenance.

## Modules

### Eureka Server

- **Description**: The Eureka Server is the backbone of the microservices architecture, providing service discovery for all other services. Each microservice registers with the Eureka Server, allowing them to locate and communicate with each other dynamically.
- **Port**: `8761`

### User Service

- **Description**: The User Service handles all user-related operations, including user registration, login, and authentication. It uses JSON Web Tokens (JWT) for securing its endpoints.
- **Port**: `8080`
- **Database**: `user_db`

### Product Service

- **Description**: The Product Service is responsible for managing the product catalog. It provides endpoints for creating, retrieving, updating, and deleting products. This service is also secured with JWT and integrates with AWS S3 for product image storage.
- **Port**: `8081`
- **Database**: `product_db`

## Technologies Used

- **Spring Boot**: For building the microservices.
- **Spring Cloud Netflix Eureka**: For service discovery.
- **Spring Data JPA**: For database interactions.
- **Spring Security**: For authentication and authorization.
- **MySQL**: As the database for both the User and Product services.
- **JSON Web Tokens (JWT)**: For securing the application.
- **Maven**: For dependency management.
- **Lombok**: To reduce boilerplate code.
- **AWS S3**: For storing product images.

## Prerequisites

- Java 21
- Maven
- MySQL
- AWS Account with S3 bucket

## Getting Started

1. **Clone the repository**:
   ```bash
   git clone https://github.com/your-username/ScaleMart.git
   ```

2. **Configure the services**:
   - Update the `application.yml` file in each service (`user-service` and `product-service`) with your MySQL database credentials.
   - In the `product-service`, configure your AWS credentials and S3 bucket name in the `application.yml` file.

3. **Build the services**:
   - Navigate to the root directory of each service and run:
     ```bash
     mvn clean install
     ```

4. **Run the services**:
   - Start the Eureka Server first.
   - Then, start the User Service and the Product Service.
   - You can run each service from your IDE or by using the following command in the service's root directory:
     ```bash
     mvn spring-boot:run
     ```

## API Endpoints

### User Service Endpoints

- `POST /api/v1/auth/register`: Register a new user.
- `POST /api/v1/auth/login`: Authenticate a user and get a JWT token.

### Product Service Endpoints

- `POST /api/v1/products`: Create a new product (requires authentication).
- `GET /api/v1/products`: Get a list of all products.
- `GET /api/v1/products/{id}`: Get a product by its ID.
- `PUT /api/v1/products/{id}`: Update a product (requires authentication).
- `DELETE /api/v1/products/{id}`: Delete a product (requires authentication).

## Configuration

Each service has its own `application.yml` file for configuration. Key properties to configure include:

- **Database connection**: `spring.datasource.url`, `spring.datasource.username`, `spring.datasource.password`
- **Server port**: `server.port`
- **Eureka server URL**: `eureka.client.service-url.defaultZone`
- **JWT secret**: `jwt.secret`
- **AWS credentials**: `aws.accessKey`, `aws.secretKey`, `aws.region`, `aws.bucketName`

## Database

The User Service and Product Service use separate MySQL databases (`user_db` and `product_db`, respectively). The application will automatically create the databases if they do not exist, as configured in the `spring.datasource.url` property.

## Security

The User Service and Product Service are secured using JWT. To access protected endpoints, you need to include a valid JWT in the `Authorization` header of your request:

```
Authorization: Bearer <your-jwt-token>
```
