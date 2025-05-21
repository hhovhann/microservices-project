# Microservices Project 🚀

This is a full-stack microservices-based demo project built with:

- **Spring Boot 3 (Java 21)**
- **Spring AI**
- **Kafka**, **Redis**, **PostgreSQL**
- **Next.js (React)** for frontend
- **Eureka**, **Spring Cloud Gateway**, and optional Spring Config Server

The project follows a modular and scalable architecture ideal for learning, demos, and resume enhancement.

---

## 🔧 Services Overview

| Service               | Stack                      | Description                                                                 |
|-----------------------|----------------------------|-----------------------------------------------------------------------------|
| `config-service`      | Spring Cloud Config        | Central configuration server for all services                              |
| `discovery-service`   | Eureka Server              | Service registry for microservice discovery                                |
| `gateway-service`     | Spring Cloud Gateway       | Entry point for external traffic with routing and load balancing           |
| `user-service`        | Spring Boot + PostgreSQL   | Manages users, persists user data, produces events to Kafka                |
| `notification-service`| Spring Boot + Kafka        | Listens to Kafka events and sends email/SMS/push notifications             |
| `chat-service`        | Spring Boot + Spring AI    | LLM-powered chat microservice using Spring AI                              |
| `frontend`            | Next.js (React)            | Web frontend UI                                                             |

---

🚀 Recommended Startup Order
1. config-service - Centralized config server that provides configs to all other services.

2. discovery-service (Eureka) - Registers and manages all microservices for discovery.

3. gateway-service - Routes requests and depends on service discovery.

4. kafka + zookeeper - Required for event streaming between services.

5. user-service - Core service producing Kafka events, registered in Eureka.

6. notification-service - Consumes Kafka events and sends notifications.

7. chat-service - LLM-powered chat backend.

8. frontend - React UI consuming backend APIs via gateway.

---


## Software Behaviour

This is a Spring Boot microservices application with the Spring Cloud. Add more later

---

## Software Manual Testing
- Testing the Game with Authentication:
  -You can now test the following scenarios:
    - Register a User: Send a POST /register request with a username and password.
      ```
        curl -X POST http://localhost:8081/v1/api/register \
             -H "Content-Type: application/json" \
             -d '{"username": "user123", "password": "password123"}'
      ```
      ```
        {
          "userId": "1982c718-d8df-44fe-a01e-188a501d00e5",
          "username": "hhovhann",
          "email": "haik.hovhannisyan@gmail.com"
        }
      ```
- Authenticate a User: Send a POST /authenticate request with valid credentials to receive a JWT token.
    ```
    curl -X POST http://localhost:8081/v1/api/authenticate \
         -H "Content-Type: application/json" \
         -d '{"username": "user123", "password": "password123"}'
    ```
  ```eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJoaG92aGFubiIsImlhdCI6MTcyNjYwNjYzOCwiZXhwIjoxNzI2NjQyNjM4fQ.OT2hT41R2Opi4dZA0uOrHK0OZKqGg-Iq2511sqZozLI```
- 
## 📦 Architecture Diagram

