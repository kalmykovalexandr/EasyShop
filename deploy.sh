#!/bin/bash

# EasyShop Simple Deploy Script
# Usage: ./deploy.sh [service1] [service2] ...
# Examples:
#   ./deploy.sh                    # Deploy all services
#   ./deploy.sh frontend           # Deploy only frontend
#   ./deploy.sh auth-service api-gateway  # Deploy specific services

set -e

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

print() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
    exit 1
}

header() {
    echo -e "${BLUE}================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}================================${NC}"
}

# Check if we're in the right directory
if [ ! -f "backend/pom.xml" ] || [ ! -f "frontend/package.json" ]; then
    error "Please run this script from the project root directory"
fi

# Parse arguments
if [ $# -eq 0 ]; then
    SERVICES=("auth-service" "product-service" "purchase-service" "api-gateway" "frontend")
else
    SERVICES=("$@")
fi

print "Services to deploy: ${SERVICES[*]}"

# Build Docker images
header "Building Docker Images"

for service in "${SERVICES[@]}"; do
    case $service in
        "auth-service")
            print "Building auth-service..."
            docker build -t easyshop-auth-service:latest -f backend/auth-service/Dockerfile backend/
            ;;
        "product-service")
            print "Building product-service..."
            docker build -t easyshop-product-service:latest -f backend/product-service/Dockerfile backend/
            ;;
        "purchase-service")
            print "Building purchase-service..."
            docker build -t easyshop-purchase-service:latest -f backend/purchase-service/Dockerfile backend/
            ;;
        "api-gateway")
            print "Building api-gateway..."
            docker build -t easyshop-api-gateway:latest -f backend/api-gateway/Dockerfile backend/
            ;;
        "frontend")
            print "Building frontend..."
            docker build -t easyshop-frontend:latest -f frontend/Dockerfile frontend/
            ;;
        *)
            error "Unknown service: $service"
            ;;
    esac
    print "✓ $service built successfully"
done

# Create simple docker-compose file
header "Creating Docker Compose Configuration"

cat > docker-compose-simple.yml << 'EOF'
services:
  db:
    image: postgres:16
    environment:
      POSTGRES_DB: easyshop
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres -d easyshop"]
      interval: 5s
      timeout: 3s
      retries: 10

  auth-service:
    image: easyshop-auth-service:latest
    environment:
      DB_URL: jdbc:postgresql://db:5432/easyshop
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/easyshop
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres
      SPRING_FLYWAY_SCHEMAS: auth
      SPRING_FLYWAY_DEFAULT_SCHEMA: auth
      SPRING_FLYWAY_CREATE_SCHEMAS: "true"
      JWT_SECRET: c3VwZXItc2VjcmV0LWp3dC1rZXktZm9yLWVhc3lzaG9wLWFwcGxpY2F0aW9uLW1pbmltdW0tMzItY2hhcmFjdGVycw==
      JWT_TTL_MINUTES: 60
    depends_on:
      db: { condition: service_healthy }
    ports:
      - "9001:9001"

  product-service:
    image: easyshop-product-service:latest
    environment:
      DB_URL: jdbc:postgresql://db:5432/easyshop
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/easyshop
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres
      SPRING_FLYWAY_SCHEMAS: products
      SPRING_FLYWAY_DEFAULT_SCHEMA: products
      SPRING_FLYWAY_CREATE_SCHEMAS: "true"
      JWT_SECRET: c3VwZXItc2VjcmV0LWp3dC1rZXktZm9yLWVhc3lzaG9wLWFwcGxpY2F0aW9uLW1pbmltdW0tMzItY2hhcmFjdGVycw==
    depends_on:
      db: { condition: service_healthy }
    ports:
      - "9002:9002"

  purchase-service:
    image: easyshop-purchase-service:latest
    environment:
      DB_URL: jdbc:postgresql://db:5432/easyshop
      SPRING_DATASOURCE_URL: jdbc:postgresql://db:5432/easyshop
      SPRING_DATASOURCE_USERNAME: postgres
      SPRING_DATASOURCE_PASSWORD: postgres
      SPRING_FLYWAY_SCHEMAS: purchases
      SPRING_FLYWAY_DEFAULT_SCHEMA: purchases
      SPRING_FLYWAY_CREATE_SCHEMAS: "true"
      JWT_SECRET: c3VwZXItc2VjcmV0LWp3dC1rZXktZm9yLWVhc3lzaG9wLWFwcGxpY2F0aW9uLW1pbmltdW0tMzItY2hhcmFjdGVycw==
      JWT_TTL_MINUTES: 60
      PRODUCT_SERVICE_URL: http://product-service:9002
    depends_on:
      db: { condition: service_healthy }
      product-service: { condition: service_started }
    ports:
      - "9003:9003"

  api-gateway:
    image: easyshop-api-gateway:latest
    environment:
      AUTH_URL: http://auth-service:9001
      PRODUCT_URL: http://product-service:9002
      PURCHASE_URL: http://purchase-service:9003
      JWT_SECRET: c3VwZXItc2VjcmV0LWp3dC1rZXktZm9yLWVhc3lzaG9wLWFwcGxpY2F0aW9uLW1pbmltdW0tMzItY2hhcmFjdGVycw==
    depends_on:
      auth-service: { condition: service_started }
      product-service: { condition: service_started }
      purchase-service: { condition: service_started }
    ports:
      - "8080:8080"

  frontend:
    image: easyshop-frontend:latest
    depends_on:
      api-gateway: { condition: service_started }
    ports:
      - "80:80"

volumes:
  pgdata:
EOF

# Deploy with Docker Compose
header "Deploying Services"

print "Stopping existing containers..."
docker compose -f docker-compose-simple.yml down 2>/dev/null || true

print "Starting services..."
docker compose -f docker-compose-simple.yml up -d

print "Deployment completed!"
print "Services available at:"
print "  Frontend: http://localhost"
print "  API Gateway: http://localhost:8080"
print "  Auth Service: http://localhost:9001"
print "  Product Service: http://localhost:9002"
print "  Purchase Service: http://localhost:9003"
print ""
print "Check status: docker compose -f docker-compose-simple.yml ps"
print "View logs: docker compose -f docker-compose-simple.yml logs -f"
