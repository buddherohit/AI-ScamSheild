# ==============================================================================
# Stage 1: Build Phase
# ==============================================================================
FROM node:20-alpine AS builder

WORKDIR /app

# Copy dependency configs
COPY package.json ./
# Run install using cache optimizations
RUN npm install

# Copy source and configurations
COPY tsconfig.json tsconfig.node.json vite.config.ts eslint.config.js ./
COPY src/ ./src
COPY index.html ./

# Build arguments for Vite compilation
ARG VITE_API_BASE_URL
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL

# Build client project
RUN npm run build

# ==============================================================================
# Stage 2: Runtime Production Phase
# ==============================================================================
FROM nginx:1.25-alpine

# Copy built artifacts from Stage 1
COPY --from=builder /app/dist /usr/share/nginx/html

# Copy routing configurations
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
