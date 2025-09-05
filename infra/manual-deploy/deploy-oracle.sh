#!/bin/bash

# EasyShop Deploy Script for Oracle Ubuntu Server
# Usage: ./deploy-oracle.sh [services...]
# 
# Available services:
#   auth-service     - Authentication service
#   product-service  - Product management service  
#   purchase-service - Purchase management service
#   api-gateway      - API Gateway
#   frontend         - React frontend
#   all              - All services (default)
#
# Examples:
#   ./deploy-oracle.sh                    # Deploy all services
#   ./deploy-oracle.sh frontend           # Deploy only frontend
#   ./deploy-oracle.sh auth-service api-gateway  # Deploy auth and gateway
#   ./deploy-oracle.sh all                # Deploy all services

set -e

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

ENV_FILE="../.env.prod"
DOCKER_COMPOSE_FILE="../docker-compose.prod.yml"

# Available services mapping
declare -A SERVICES=(
    ["auth-service"]="easyshop-auth-service:latest ../backend/auth-service/Dockerfile ../backend/"
    ["product-service"]="easyshop-product-service:latest ../backend/product-service/Dockerfile ../backend/"
    ["purchase-service"]="easyshop-purchase-service:latest ../backend/purchase-service/Dockerfile ../backend/"
    ["api-gateway"]="easyshop-api-gateway:latest ../backend/api-gateway/Dockerfile ../backend/"
    ["frontend"]="easyshop-frontend:latest ../frontend/Dockerfile ../frontend/"
)

# Parse command line arguments
SERVICES_TO_BUILD=()
if [ $# -eq 0 ]; then
    # No arguments - build all services
    SERVICES_TO_BUILD=("auth-service" "product-service" "purchase-service" "api-gateway" "frontend")
else
    # Parse arguments
    for arg in "$@"; do
        if [ "$arg" = "all" ]; then
            SERVICES_TO_BUILD=("auth-service" "product-service" "purchase-service" "api-gateway" "frontend")
            break
        elif [[ -n "${SERVICES[$arg]}" ]]; then
            SERVICES_TO_BUILD+=("$arg")
        else
            echo -e "${RED}[ERROR]${NC} Unknown service: $arg"
            echo "Available services: auth-service, product-service, purchase-service, api-gateway, frontend, all"
            exit 1
        fi
    done
fi

print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_header() {
    echo -e "${BLUE}================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}================================${NC}"
}

# Check if we are in the correct directory
if [ ! -f "../backend/pom.xml" ] || [ ! -f "../frontend/package.json" ]; then
    print_error "Please run this script from the infra/manual-deploy/ directory"
    print_error "Current directory: $(pwd)"
    print_error "Looking for: ../backend/pom.xml and ../frontend/package.json"
    exit 1
fi

# Check if required files exist
if [ ! -f "$DOCKER_COMPOSE_FILE" ]; then
    print_error "Docker compose file not found: $DOCKER_COMPOSE_FILE"
    exit 1
fi

if [ ! -f "$ENV_FILE" ]; then
    print_error "Environment file not found: $ENV_FILE"
    exit 1
fi

print_header "EasyShop Deploy for Oracle Server"
print_status "Services to build: ${SERVICES_TO_BUILD[*]}"

# Load environment variables
source "$ENV_FILE"

# Build Docker Images
print_header "Building Docker Images"

for service in "${SERVICES_TO_BUILD[@]}"; do
    if [[ -n "${SERVICES[$service]}" ]]; then
        IFS=' ' read -r image_name dockerfile build_context <<< "${SERVICES[$service]}"
        print_status "Building $service..."
        docker build -t "$image_name" -f "$dockerfile" "$build_context"
        print_status "✓ $service built successfully"
    fi
done

print_status "All specified Docker images built successfully!"

# Deploy with Docker Compose
print_header "Deploying with Docker Compose"

# Stop only the services we're rebuilding
if [ ${#SERVICES_TO_BUILD[@]} -lt 5 ]; then
    print_status "Stopping only changed services..."
    for service in "${SERVICES_TO_BUILD[@]}"; do
        case $service in
            "auth-service")
                docker compose -f "$DOCKER_COMPOSE_FILE" stop auth-service 2>/dev/null || true
                ;;
            "product-service")
                docker compose -f "$DOCKER_COMPOSE_FILE" stop product-service 2>/dev/null || true
                ;;
            "purchase-service")
                docker compose -f "$DOCKER_COMPOSE_FILE" stop purchase-service 2>/dev/null || true
                ;;
            "api-gateway")
                docker compose -f "$DOCKER_COMPOSE_FILE" stop api-gateway 2>/dev/null || true
                ;;
            "frontend")
                docker compose -f "$DOCKER_COMPOSE_FILE" stop frontend 2>/dev/null || true
                ;;
        esac
    done
else
    print_status "Stopping all services..."
    docker compose -f "$DOCKER_COMPOSE_FILE" down --remove-orphans 2>/dev/null || true
fi

print_status "Starting services..."
docker compose -f "$DOCKER_COMPOSE_FILE" --env-file "$ENV_FILE" up -d

print_status "Deployment completed!"
print_status "Check status with: docker compose -f $DOCKER_COMPOSE_FILE ps"
