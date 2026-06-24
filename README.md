# AI ScamShield

> **Detect Digital Fraud Before Money Leaves Your Account**

AI ScamShield is a production-ready, enterprise-grade application framework designed to intercept and flag digital scam patterns, suspicious transaction request chains, phishing inputs, and malicious digital transactions. This repository represents the **Phase 1 Foundation Setup** of the system, establishing a highly-scalable monorepo structure.

---

## 🛠 Technology Stack

### Frontend Component
* **Framework:** React 19+ (TypeScript strict mode)
* **Build System:** Vite
* **Design & UI:** Material UI v7
* **State Management:** Redux Toolkit (RTK)
* **Routing:** React Router v7
* **Form & Validation:** React Hook Form + Zod
* **HTTP Client:** Axios
* **Linter & Formatting:** ESLint (Flat Config) + Prettier

### Backend Component
* **Language Runtime:** Java 21
* **Framework:** Spring Boot 3.5+
* **Security:** Spring Security (JWT-ready, Stateless)
* **Database & Persistence:** MySQL 8.4 LTS + Spring Data JPA + Flyway migrations
* **Utilities:** MapStruct (mappings), Lombok, Jakarta Validation API
* **API Documentation:** OpenAPI 3 / Swagger UI

### Infrastructure & Deployment
* **Orchestration:** Docker Compose
* **Database:** MySQL 8.4 LTS
* **Web Server:** Nginx (multi-stage build serving React SPA)

---

## 📂 Repository Directory Layout

```text
ai-scamshield/
├── backend/                   # Spring Boot 3.5 Web Application (Maven)
├── frontend/                  # React 19 Client SPA (Vite + TypeScript)
├── docs/                      # Technical Architecture & Setup Documents
├── infrastructure/            # Docker, Nginx, and cloud config files
├── scripts/                   # Shell and PowerShell developer scripts
├── .editorconfig              # Code formatting settings across IDEs
├── .gitignore                 # Files excluded from git tracking
├── .env.example               # Environment variables template
├── docker-compose.yml         # Container orchestration configuration
└── README.md                  # Project overview and developer roadmap
```

---

## 🚀 Quick Start Guide

### Prerequisites
* [Java Development Kit (JDK) 21](https://adoptium.net/temurin/releases/?version=21)
* [Node.js v20+](https://nodejs.org/) (npm v10+)
* [Docker Desktop](https://www.docker.com/products/docker-desktop/)
* [Apache Maven v3.9+](https://maven.apache.org/)

### Local Environment Setup

1. **Clone the Repository**
   ```bash
   git clone https://github.com/your-org/ai-scamshield.git
   cd ai-scamshield
   ```

2. **Initialize Environment Configuration**
   Copy the example environment file to `.env`:
   ```bash
   copy .env.example .env
   ```

3. **Start Core Infrastructure**
   Launch MySQL container:
   ```bash
   docker compose up mysql -d
   ```

4. **Run the Backend Services**
   Navigate to the backend directory and launch the Spring Boot app:
   ```bash
   cd backend
   mvn spring-boot:run
   ```
   * The API will boot on: `http://localhost:8080`
   * Swagger Documentation is available at: `http://localhost:8080/swagger-ui/index.html`

5. **Run the Frontend Application**
   Navigate to the frontend directory, install dependencies, and start Vite:
   ```bash
   cd ../frontend
   npm install
   npm run dev
   ```
   * The application UI will boot on: `http://localhost:5173`

---

## 🐳 Dockerized Deployment (Production Style)

To compile and launch the entire monorepo stack using Docker:
```bash
docker compose up --build -d
```
* **Frontend SPA:** `http://localhost` (served via Nginx)
* **Backend REST API:** `http://localhost:8080`
* **MySQL Database:** `localhost:3306`

---

## 🛡 Clean Architecture Philosophy

* **Separation of Concerns:** Business layers contain zero framework-specific constructs.
* **Strict DTO Mapping:** Separation of persistence models (`@Entity`) from presentation payloads (`DTO`) via MapStruct.
* **Global Error Envelope:** Structured JSON response model used universally across HTTP responses.
* **Database Versioning:** All schema changes are controlled using Flyway migration scripts located in `db/migration/`.
