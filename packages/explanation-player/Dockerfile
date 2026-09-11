# ============================================================
# Stage 1: Build explanation-player UI
# ============================================================
FROM node:20-alpine AS build

WORKDIR /app

# Install dependencies using clean install
COPY package.json package-lock.json* ./
RUN npm ci

# Copy application source and configuration
COPY . .

# Build application assets into dist-app
RUN npm run build

# ============================================================
# Stage 2: Serve static assets with Nginx
# ============================================================
FROM nginx:alpine

# Remove default nginx static assets
RUN rm -rf /usr/share/nginx/html/*

# Copy built application from build stage
COPY --from=build /app/dist-app /usr/share/nginx/html

# Copy custom Nginx configuration with API reverse proxy
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
