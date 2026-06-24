# Navigation & Routes Folder (`src/routes/`)

This folder manages URL routing and access guards:

* **`index.tsx`**: Defines public routes, protected routes, and layouts mapping.
* **ProtectedRoute**: Limits access to authenticated users.
* **PublicRoute**: Limits access to unauthenticated guests (e.g., redirects active sessions away from `/login`).
