# Network & API Services Folder (`src/services/`)

This directory encapsulates all network logic, API gateways, external service integrations, and connection initializers.

## Key Files

* **`apiClient.ts`**: The main Axios configuration incorporating interceptors for automatic JWT injection, handling refresh tokens, and reporting network failures.
* **`socketClient.ts`**: (Future Phase) Connection manager for WebSocket threat notifications.
