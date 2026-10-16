# Hotel Management Backend

A Spring Boot REST API for managing hotel listings, room inventory, user authentication, bookings, and secure payment workflows for a hotel booking platform.

## Overview

This backend provides the core services for a hotel booking application, including:

- Hotel creation, activation, and management
- Room inventory and availability updates
- User signup/login with JWT authentication
- Booking initialization and guest management
- Stripe-based payment initiation and webhook capture
- Hotel booking reports for administrators
- Role-based access for hotel managers

## Features

- Hotel management APIs for admins
- Hotel search and information retrieval for guests
- Booking lifecycle management
- User profile and booking history endpoints
- Inventory updates for rooms
- JWT authentication with refresh-token support
- Security rules for hotel-manager access
- OpenAPI/Swagger documentation
- PostgreSQL persistence

## Tech Stack

- Java 26
- Spring Boot 4.1.1
- Spring Web MVC
- Spring Data JPA
- Spring Security
- PostgreSQL
- JWT (jjwt)
- Stripe Java SDK
- ModelMapper
- OpenAPI / Springdoc
- Maven

## Project Structure

```text
src/
  main/
    java/
      com/example/airBnbApp/
        advice/
        config/
        controller/
        dto/
        entity/
        exception/
        repository/
        security/
        service/
        strategy/
        util/
    resources/
      application.properties
  test/
    java/
      com/example/airBnbApp/
```

## Prerequisites

Before running the project, ensure you have:

- Java 26 installed
- Maven installed
- PostgreSQL running locally
- Stripe account credentials for payment integration

## Configuration

The application is configured in `src/main/resources/application.properties`.

Update the following values before running the app:

- Database connection details:
  - `spring.datasource.url`
  - `spring.datasource.username`
  - `spring.datasource.password`
- JWT secret:
  - `jwt.secretKey`
- Stripe keys:
  - `stripe.secret.key`
  - `stripe.webhook.secret`

Example configuration:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/airBnb
spring.datasource.username=postgres
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
server.servlet.context-path=/api/v1
jwt.secretKey=your_jwt_secret
stripe.secret.key=your_stripe_secret_key
stripe.webhook.secret=your_stripe_webhook_secret
```

## Running the Application

Clone the repository and run:

```bash
./mvnw spring-boot:run
```

Or with Maven:

```bash
mvn spring-boot:run
```

The application will start on:

```text
http://localhost:8080
```

With the project context path applied:

```text
http://localhost:8080/api/v1
```

## API Overview

### Authentication

- `POST /api/v1/auth/signup` — Register a new user
- `POST /api/v1/auth/login` — Login and receive JWT tokens
- `POST /api/v1/auth/refresh` — Refresh access token using refresh token cookie

### Hotels

- `GET /api/v1/hotels/search` — Search for hotels
- `GET /api/v1/hotels/{hotelId}/info` — Get hotel details

### Admin Hotel Management

- `POST /api/v1/admin/hotels` — Create hotel
- `GET /api/v1/admin/hotels` — List all hotels
- `GET /api/v1/admin/hotels/{hotelId}` — Get hotel by ID
- `PUT /api/v1/admin/hotels/{hotelId}` — Update hotel
- `DELETE /api/v1/admin/hotels/{hotelId}` — Delete hotel
- `PATCH /api/v1/admin/hotels/{hotelId}/activate` — Activate hotel
- `GET /api/v1/admin/hotels/{hotelId}/bookings` — View hotel bookings
- `GET /api/v1/admin/hotels/{hotelId}/reports` — Get hotel report

### Inventory

- `GET /api/v1/admin/inventory/rooms/{roomId}` — Get room inventory
- `PATCH /api/v1/admin/inventory/rooms/{roomId}` — Update room inventory

### Bookings

- `POST /api/v1/bookings/init` — Initialize a booking
- `POST /api/v1/bookings/{bookingId}/addGuests` — Add guest details
- `POST /api/v1/bookings/{bookingId}/payments` — Start payment session
- `POST /api/v1/bookings/{bookingId}/cancel` — Cancel booking
- `POST /api/v1/bookings/{bookingId}/status` — Get booking status

### Users

- `PATCH /api/v1/users/profile` — Update profile
- `GET /api/v1/users/myBookings` — Get current user's bookings
- `GET /api/v1/users/profile` — Get current user's profile

### Webhooks

- `POST /api/v1/webhook/payment` — Stripe payment webhook endpoint

## Security

The application uses Spring Security with JWT-based authentication.

- Public endpoints are allowed for general access
- Protected routes require authentication
- Admin routes require the `HOTEL_MANAGER` role
- Refresh tokens are stored in an HTTP-only cookie

## API Documentation

Swagger/OpenAPI documentation is enabled through Springdoc.

Open the API docs here:

```text
http://localhost:8080/api/v1/swagger-ui/index.html
```

## Notes

- The project currently contains hardcoded secrets in `application.properties` for local development and should be replaced with environment-specific secrets in production.
- The backend is designed for hotel booking workflows and can be integrated with a frontend application or mobile client.

## License

This project is currently unlicensed unless explicitly added later.
