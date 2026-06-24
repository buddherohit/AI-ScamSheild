# Folder Structure Reference Guide

This document maps out the monorepo directory layouts, explaining the architectural purpose of each root folder.

---

## 📂 Root Structure

```text
ai-scamshield/
├── backend/                   # Spring Boot 3.5 Web Application (Maven)
├── frontend/                  # React 19 Client SPA (Vite + TypeScript)
├── docs/                      # Technical Architecture & Setup Documents
├── infrastructure/            # Docker, Nginx, and cloud config files
└── scripts/                   # Shell and PowerShell developer scripts
```

---

## 💻 Frontend Layout (`frontend/`)

The React project is located in `frontend/`.

```text
frontend/src/
├── app/          # Initializers (main.tsx, App.tsx, Context Providers)
├── routes/       # Route guards, layout configurations, and router definitions
├── layouts/      # Visual containers wrapping groups of pages (Auth, Main)
├── pages/        # Route screen targets (Login, Dashboard, NotFound)
├── features/     # Modulized business features (auth, fraud, reports)
├── components/   # Presentational UI components (Buttons, Input, Cards)
├── services/     # Axios client configuration and network calling setups
├── store/        # Redux Toolkit global store configuration and slices
├── hooks/        # Reusable React custom hooks
├── utils/        # Generic functions (date formatters, validators)
├── constants/    # Fixed variables (endpoints, regex rules)
├── types/        # TypeScript models and interfaces
├── assets/       # Static graphics, SVG icons
└── theme/        # Material UI design systems and theme tokens
```

---

## ☕ Backend Layout (`backend/`)

The Java Maven project is located in `backend/`.

```text
backend/src/main/
├── java/com/scamshield/
│   ├── config/       # Configurations (CORS, Database, Jackson, OpenAPI)
│   ├── security/     # JWT Provider, Auth Filters, Security Endpoints
│   ├── common/       # ApiResponse envelop, Paginated models, Audit entities
│   ├── exception/    # Custom Exception and Controller Exception Handler
│   ├── validation/   # Jakarta validation utilities
│   ├── entity/       # Database Entities
│   ├── dto/          # Serialization request and response payloads
│   ├── repository/   # Spring Data JPA repositories
│   ├── service/      # Business Domain Logic Interfaces
│   ├── controller/   # REST Controllers
│   ├── mapper/       # MapStruct conversion entities
│   └── util/         # Static helper classes
└── resources/
    ├── db/migration/ # Flyway database schema version scripts
    ├── application.yml # Core spring configuration
    └── application-local.yml # Local developer settings
```
