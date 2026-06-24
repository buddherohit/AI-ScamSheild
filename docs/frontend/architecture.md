# AI ScamShield Frontend Architecture

This document describes the frontend architecture and conventions for AI ScamShield Phase 3.

## Tech Stack
- **Framework**: React 19 + Vite
- **Language**: TypeScript
- **State Management**: Redux Toolkit (Auth, UI, Settings, Notifications, Sessions, Dashboard slices)
- **Routing**: React Router
- **Styling**: Tailwind CSS + shadcn/ui
- **API Client**: Axios (with centralized interceptors for token management)
- **Testing**: Vitest + React Testing Library

## Directory Structure
- `/src/components/ui`: Auto-generated shadcn/ui components (Buttons, Cards, Inputs, Dialogs). Do not modify these unless globally overriding design tokens.
- `/src/components`: Custom business logic components (e.g., `CommandPalette.tsx`).
- `/src/layouts`: Layout wrappers (`MainLayout.tsx`, `AuthLayout.tsx`, `Sidebar.tsx`, `Header.tsx`).
- `/src/pages`: Top-level route views (`Dashboard.tsx`, `Profile.tsx`, `Login.tsx`).
- `/src/store/slices`: Redux slices handling global app state.
- `/src/services`: API abstraction layer (e.g., `apiClient.ts`, `dashboardService.ts`).
- `/src/routes/index.tsx`: Application route orchestrator and auth guards.

## Design System
We follow a premium SaaS aesthetic using Tailwind CSS.
- **Theme Variables**: Defined in `src/index.css`.
- **Colors**: Primarily using HSL colors. Includes full support for light and dark modes.
- **Components**: Leverages accessible components from shadcn/ui. 

## State Management
Redux Toolkit is the primary state container.
- Use `useAppSelector` and `useAppDispatch` from `@/store` to interact with state.
- Asynchronous data fetching is wrapped in `src/services` and then dispatched to Redux reducers.

## Testing
- Tests are co-located with their respective components/files (`*.test.tsx`).
- Run tests via `npm run test` (Vitest).
