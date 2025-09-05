#!/bin/bash

# EasyShop Production Deploy Script
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
YELLOW='\033[1;33m'
NC='\033[0m'

print() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1"
    exit 1
}

warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

header() {
    echo -e "${BLUE}================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}================================${NC}"
}

# Check if we're in the right directory (infra folder)
if [ ! -f "../backend/pom.xml" ] || [ ! -f "../frontend/package.json" ]; then
    error "Please run this script from the infra directory"
fi

# Check for .env file
if [ ! -f ".env" ]; then
    error ".env file not found! Please create it manually with your actual values"
fi

# Load environment variables
export $(cat .env | grep -v '^#' | xargs)

# Validate required environment variables
if [ -z "$DB_USER" ] || [ -z "$DB_PASSWORD" ] || [ -z "$JWT_SECRET" ]; then
    error "Missing required environment variables in .env file"
fi

# Parse arguments
if [ $# -eq 0 ]; then
    SERVICES=("auth-service" "product-service" "purchase-service" "api-gateway" "frontend")
else
    SERVICES=("$@")
fi

print "Services to deploy: ${SERVICES[*]}"
print "Using Docker registry: ${DOCKER_REGISTRY:-local}"
print "Using image version: ${IMAGE_VERSION:-latest}"

# Build Docker images
header "Building Docker Images"

for service in "${SERVICES[@]}"; do
    case $service in
        "auth-service")
            print "Building auth-service..."
            docker build -t ${DOCKER_REGISTRY:-easyshop}/auth-service:${IMAGE_VERSION:-latest} -f ../backend/auth-service/Dockerfile ../backend/
            ;;
        "product-service")
            print "Building product-service..."
            docker build -t ${DOCKER_REGISTRY:-easyshop}/product-service:${IMAGE_VERSION:-latest} -f ../backend/product-service/Dockerfile ../backend/
            ;;
        "purchase-service")
            print "Building purchase-service..."
            docker build -t ${DOCKER_REGISTRY:-easyshop}/purchase-service:${IMAGE_VERSION:-latest} -f ../backend/purchase-service/Dockerfile ../backend/
            ;;
        "api-gateway")
            print "Building api-gateway..."
            docker build -t ${DOCKER_REGISTRY:-easyshop}/api-gateway:${IMAGE_VERSION:-latest} -f ../backend/api-gateway/Dockerfile ../backend/
            ;;
        "frontend")
            print "Building frontend..."
            docker build -t ${DOCKER_REGISTRY:-easyshop}/frontend:${IMAGE_VERSION:-latest} -f ../frontend/Dockerfile ../frontend/
            ;;
        *)
            error "Unknown service: $service"
            ;;
    esac
    print "✓ $service built successfully"
done

# Deploy with Docker Compose using production config
header "Deploying Services"

print "Using production Docker Compose configuration from docker-compose.yml"

print "Stopping existing containers..."
docker compose -f docker-compose.yml down 2>/dev/null || true

print "Starting services..."
docker compose -f docker-compose.yml up -d

print "Deployment completed!"
print "Services available at:"
print "  Frontend: http://localhost"
print "  API Gateway: http://localhost:8080"
print "  Auth Service: http://localhost:9001"
print "  Product Service: http://localhost:9002"
print "  Purchase Service: http://localhost:9003"
print ""
print "Check status: docker compose -f docker-compose.yml ps"
print "View logs: docker compose -f docker-compose.yml logs -f"
print "Stop services: docker compose -f docker-compose.yml down"
