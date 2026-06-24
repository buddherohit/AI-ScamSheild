# Redux Toolkit Store Folder (`src/store/`)

This folder manages the global state of the application using Redux Toolkit (RTK).

## Architecture

* **`index.ts`**: Configures the Redux store, registers standard middleware, and exports typescript typings for standard hooks.
* **`slices/`**: Localized state reducers:
  * `authSlice.ts`: Tracks authentication state, JWT tokens, and user credentials.
  * `uiSlice.ts`: Orchestrates responsive sidebars, theme modes, and centralized notification snackbars.
