# AI ScamShield - Technical Architecture Document

This document details the software architecture, design principles, security protocols, and operational workflows for the **AI ScamShield** platform.

---

## 🏛 Overall Architectural Design

AI ScamShield is structured as a **Scalable Monolith** to enable high developer efficiency, atomic codebase updates, and direct sharing of API contracts. It is carefully structured to remain **Future Microservice Ready** by grouping domain features into distinct boundaries.

### Architecture Topology

```text
                               ┌──────────────────────┐
                               │     Web Browser      │
                               └──────────┬───────────┘
                                          │ http://localhost:80
                                          ▼
                         ┌──────────────────────────────────┐
                         │      Nginx Gateway Router        │
                         └──────┬────────────────────┬──────┘
                                │                    │
              (Static Assets)   │                    │  /api/v1 (API Proxy)
                                ▼                    ▼
                     ┌──────────┴─────────┐ ┌────────┴──────────┐
                     │   Frontend (SPA)   │ │   Backend REST    │
                     │    Nginx Server    │ │    Spring Boot    │
                     └────────────────────┘ └────────┬──────────┘
                                                     │ JDBC
                                                     ▼
                                            ┌────────┴──────────┐
                                            │   MySQL 8.4 LTS   │
                                            └───────────────────┘
```

---

## 🛡 Frontend Architecture (`frontend/`)

The frontend application uses React 19 and TypeScript, built using Vite. Global state is managed via Redux Toolkit, and UI is styled with Material UI v7.

### Design Principles
* **State/Logic Decoupling:** API states, authorizations, and notification overlays are managed inside Redux slices. Presentational components remain pure and stateless.
* **Type-Safe Payloads:** Schema validations are executed at the presentation layer using Zod, ensuring zero invalid forms are submitted to API routes.
* **Centralized API Layer:** Interceptors validate access tokens, trigger automatic refreshes when a `401 Unauthorized` occurs, and standardize error structures.

---

## ☕ Backend Architecture (`backend/`)

The backend is built with Spring Boot 3.5+ and Java 21, conforming to **Clean Architecture** patterns.

```text
 ┌─────────────────────────────────────────────────────────────┐
 │ Presentation Layer (REST Controllers, Global Exception)      │
 └───────────────┬─────────────────────────────────────────────┘
                 ▼
 ┌─────────────────────────────────────────────────────────────┐
 │ Application Layer (Services, Request Validations, DTOs)     │
 └───────────────┬─────────────────────────────────────────────┘
                 ▼
 ┌─────────────────────────────────────────────────────────────┐
 │ Domain Layer (JPA Entities, Base Entities, Custom Audits)   │
 └─────────────────────────────────────────────────────────────┘
```

### Key Architectural Guidelines
1. **Separation of Concerns:** Business layers contain zero framework-specific constructs.
2. **DTO Mapping Rules:** Entities are never returned directly to endpoints. MapStruct translates Entities to/from DTOs.
3. **Stateless Security:** Request filtering checks authorization tokens on each endpoint. Security Context is stored thread-local per request execution.
4. **Structured Exception Envelopes:** Handlers catch runtime anomalies and return consistent, typed error payloads matching the `{success, message, data, timestamp}` model.
