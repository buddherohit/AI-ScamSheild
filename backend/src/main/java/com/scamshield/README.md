# Backend Clean Architecture Structure (`com.scamshield`)

This directory contains the Spring Boot enterprise application code. The package design follows **Clean Architecture** principles to separate core business rules from infrastructure details and controllers.

---

## 🏛 Package Categories

### Core Architectural Layers

* **`config/`**: System initializers, CORS settings, database auditories, serialization specs, and Swagger setups.
* **`security/`**: Stateless Spring Security filters, custom JWT decoders, and exception writers.
* **`common/`**: Platform wrappers like `ApiResponse` envelope, audits (`BaseEntity`), and paged listings.
* **`exception/`**: Global REST controller exception handler, and platform-specific exceptions (ResourceNotFound, Business, Validation).
* **`validation/`**: Custom validation annotations and validation payloads.
* **`dto/`**: Data Transfer Objects for serialization. Includes requests, responses, and paginated wrappers.
* **`entity/`**: Database persistence entities mapping database tables to JPA.
* **`repository/`**: JPA data-access interfaces.
* **`service/`**: Core business domain logic, interfaces, and transaction controllers.
* **`controller/`**: REST Controllers exposing the endpoints.
* **`mapper/`**: MapStruct mappings translating entities to DTOs.
* **`util/`**: Standalone helper classes (string helpers, calculations, date tools).

---

## 🛡 Modular Feature Directory

For future expansion, new logic should be modularized inside feature packages rather than global directories if it represents a cohesive sub-domain. Placeholders:

* **`auth/`**: Custom user registration, credential checking, token refreshes.
* **`fraud/`**: Heuristic scam evaluation rules, real-time transaction interceptors.
* **`report/`**: Audit tracking exports, threat compliance.
* **`notification/`**: Warning dispatches.
* **`analytics/`**: Telemetry log aggregations.

---

## 🔄 Layer Separation & Data Flow

```text
Request (JSON) ──> Controller ──> Service ──> Repository ──> MySQL
                       │             │            │
                  Accept DTO    Execute Rules  Fetch Entity
                       │             │            │
Response (JSON) <── ApiResponse <── Map DTO <── Return Entity
```

1. **DTOs** are consumed by the **Controller** layer.
2. The **Controller** invokes the **Service** layer.
3. The **Service** operates on business logic and invokes the **Repository** layer.
4. **Repository** maps queries to database **Entities**.
5. **Mappers** transform database **Entities** to response DTOs, which are wrapped in `ApiResponse` and returned by the **Controller**.
