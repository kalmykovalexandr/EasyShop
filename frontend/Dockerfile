FROM node:20-alpine AS build
WORKDIR /app
COPY package.json package-lock.json* ./
RUN npm ci --quiet
COPY . .
RUN /bin/sh -c 'if [ -f package-lock.json ]; then npm ci --quiet; else npm install --quiet; fi'
FROM nginx:1.27-alpine
WORKDIR /usr/share/nginx/html
COPY --from=build /app/dist .
