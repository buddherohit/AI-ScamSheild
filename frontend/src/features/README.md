# Feature Architecture Folder (`src/features/`)

This directory is the core modular domain layer of the frontend application. It follows a feature-based structure to ensure scalability, loose coupling, and high cohesion.

## Module Structure

Each feature folder is self-contained and should ideally encapsulate its own components, logic, and state:

* **`auth/`**: Sign-in, sign-up, password reset, and registration page flows.
* **`dashboard/`**: Core security metrics visualization, widgets, system configuration toggles.
* **`fraud/`**: Threat monitoring, transaction list views, scanning sandboxes, heuristic rule managers.
* **`reports/`**: Analytics grids, export builders, threat event logs, audit trackers.
* **`notifications/`**: Dispatch channels, webhooks setups, warning preferences dashboards.

## Internal Directory Guidelines

When implementing a feature, you can structure it as follows:

```text
features/my-feature/
├── components/          # Reusable UI components private to this feature
├── hooks/               # Custom React hooks specific to this feature
├── services/            # Axios API calling modules for this feature
├── slices/              # Redux slices/actions localized to this feature
├── types.ts             # TypeScript interfaces for this feature
└── index.ts             # Public API entry point for outer imports
```
