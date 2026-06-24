# Shared UI Components Folder (`src/components/`)

This directory contains presentational components that are shared across different feature modules. 

## Design Guidelines

* **Zero Feature Domain Knowledge:** Components here should not know about specific feature logic or Redux state. They should receive data through props and report actions through callbacks.
* **Highly Reusable:** Designed to be generic (e.g., custom `Button`, `DataTable`, `CardWrapper`, `LoadingOverlay`, `FormTextField`).
* **Styled via Theme Tokens:** Leverage Material UI v7 styling APIs and use HSL color tokens to inherit style rules automatically.
