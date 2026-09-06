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

---

## 🧪 Testing the synchronous flow

All calls go through the **API Gateway on port 8080**.  
Make sure the four services are running first.

### 0) Check the preloaded data

Product and Client each preload their H2 database from a `data.sql`, so there is nothing to create before testing.

```bash
curl http://localhost:8080/clients
```

```bash
curl http://localhost:8080/products
```

Note the ids and stock levels returned — the examples below assume client `1`, products `1` and `2`. Adjust them to match your `data.sql`.

> Since the databases are in-memory, restarting a service resets it to its `data.sql` state. Handy: restart Product to reset stock between test runs.

---

### 1) Nominal case

```bash
curl -i -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"clientId":1,"items":[{"productId":1,"quantity":2},{"productId":2,"quantity":1}]}'
```

**Expected:** `201 Created` with the order payload.

Then check that the stock was decremented **immediately**:

```bash
curl http://localhost:8080/products/1
```

**Expected:** `stock` dropped by 2 right away.  
This is the proof the reservation is synchronous — in an async version the stock would still read the old value for a few milliseconds.

---

### 2) Error mapping

| Case | Expected status | Body code |
| --- | --- | --- |
| Missing client | `404` | `CLIENT_NOT_FOUND` |
| Missing product | `404` | `PRODUCT_NOT_FOUND` |
| Insufficient stock | `409` | `INSUFFICIENT_STOCK` |

**Missing client**

```bash
curl -i -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"clientId":999,"items":[{"productId":1,"quantity":1}]}'
```

**Missing product**

```bash
curl -i -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"clientId":1,"items":[{"productId":999,"quantity":1}]}'
```

**Insufficient stock**

```bash
curl -i -X POST http://localhost:8080/orders \
  -H "Content-Type: application/json" \
  -d '{"clientId":1,"items":[{"productId":2,"quantity":100}]}'
```

> No `500` should ever appear on these flows.

---

## H2 consoles (dev only)

- Product: http://localhost:8081/h2-console
- Client: http://localhost:8082/h2-console
- Order: http://localhost:8083/h2-console

## 🧠 Design notes

- Sync orchestration: Order service uses WebClient to fetch Client and Product, then reserves stock synchronously. 
- Validate-then-act: `OrderService.create()` resolves the client, then fetches **and checks** every product before issuing any reservation. Anything that can fail happens before anything that cannot be undone.
- Error mapping:
  - Missing client/product → 404 
  - Insufficient stock → 409 
  - Avoid generic 500 on normal flows.
- DI / layering: Controller → Service (port-in) → Repository port (port-out) → JPA adapter (infrastructure). 
- Databases: Separate H2 in-memory per service (unique JDBC URLs); `data.sql` preload for Product and Client.

## ⚠️ Known limitations

- **No distributed transaction.** If a reservation ever succeeded and a later one failed, the reserved stock would not be released. The two-phase validation avoids this in practice, but the guarantee is not absolute — a compensating `release` call would be needed for that.
- **Race window.** Between validation and reservation, a concurrent order can consume the stock. Solving this properly requires locking or an atomic multi-product reservation on the Product side.
- **Duplicate product ids.** Two lines referencing the same `productId` are validated independently, so their combined quantity is not checked against the stock.

These are exactly the constraints the async version addresses.
