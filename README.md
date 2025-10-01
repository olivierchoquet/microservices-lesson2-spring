# Microservices Lesson 2 — Spring Boot (Gateway + Product + Client + Order)

Synchronous microservices baseline for students.  
**Architecture:** API Gateway → Product → Client → Order (HTTP sync).  
Each service has its own H2 DB, DI with clean layers, and simple error handling. You’ll later extend this to an **async** version.

---

## 💻 Prerequisites

- **JDK 17** (required)
- **Git**
- **Maven**

## 🧱 Project layout (multi-module Maven)

microservices-lesson2/ 
├─ pom.xml # parent/aggregator POM\
├─ api-gateway/ # Spring Cloud Gateway (port 8080)\
├─ product/ # Product Service (port 8081)\
├─ client/ # Client Service (port 8082)\
└─ order/ # Order Service (port 8083)\

**Ports**
- Gateway → **8080**
- Product → **8081**
- Client  → **8082**
- Order   → **8083**

---

## 🚀 Quick Start

### 1) Clone

```
git clone https://github.com/Aktorius/microservices-lesson2-spring.git
cd microservices-lesson2
```
### 2) Run the Services

1. Open microservices-lesson2 in IntelliJ → Maven auto-import detects 4 modules. 
2. Run each main class (green ▶):
   - product/.../ProductServiceApplication 
   - client/.../ClientServiceApplication 
   - order/.../OrderServiceApplication 
   - api-gateway/.../ApiGatewayApplication

You should see:
   - Product: Tomcat started on port(s): 8081 
   - Client: Tomcat started on port(s): 8082 
   - Order: Tomcat started on port(s): 8083 
   - Gateway: Netty started on port(s): 8080

## 🔌 API (via Gateway on 8080)

### Clients 
  - GET /clients — list all 
  - GET /clients/{id} — get one (404 if missing)
  - POST /clients — create 
    ```json
    { "name": "Alice", "address": "Brussels" }
    ```
  - PUT /clients/{id} — update 
  - DELETE /clients/{id} — delete

### Products
- GET /products 
- GET /products/{id} 
- POST /products — create
    ```json
    { "name": "Keyboard", "price": 49.90, "stock": 10 }
    ```
- PUT /products/{id} 
- DELETE /products/{id} 
- POST /products/{id}/reserve — reserve stock
    ```json
    { "quantity": 2 }
    ```
    → returns 409 if insufficient stock

### Orders

- POST /orders — create order
    ```json
    {
        "clientId": 1,
        "items": [
            { "productId": 1, "quantity": 2 },
            { "productId": 2, "quantity": 1 }
        ]
    }
    ```
- GET /orders — list all orders 
- GET /orders?clientId=1 — list by client 
- GET /orders/{id} — get one

## H2 consoles (dev only)

- Product: http://localhost:8081/h2-console
- Client: http://localhost:8082/h2-console
- Order: http://localhost:8083/h2-console

## 🧠 Design notes

- Sync orchestration: Order service uses WebClient to fetch Client and Product, then reserves stock synchronously. 
- Error mapping:
  - Missing client/product → 404 
  - Insufficient stock → 409 
  - Avoid generic 500 on normal flows.
- DI / layering: Controller → Service (port-in) → Repository port (port-out) → JPA adapter (infrastructure). 
- Databases: Separate H2 in-memory per service (unique JDBC URLs); optional data.sql preload.