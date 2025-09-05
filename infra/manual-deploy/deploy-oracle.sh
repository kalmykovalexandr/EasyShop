#!/bin/bash

# EasyShop Complete Build and Deploy Script for Oracle Ubuntu Server
# This script performs full build and deployment of the entire EasyShop project
# Usage: ./deploy-oracle.sh [--skip-build] [--skip-deploy] [--help]

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default values
SKIP_BUILD=false
SKIP_DEPLOY=false
ENV_FILE="../.env.prod"
DOCKER_COMPOSE_FILE="../docker-compose.prod.yml"

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --skip-build)
            SKIP_BUILD=true
            shift
            ;;
        --skip-deploy)
            SKIP_DEPLOY=true
            shift
            ;;
        -h|--help)
            echo "Usage: $0 [--skip-build] [--skip-deploy] [--help]"
            echo ""
            echo "Arguments:"
            echo "  --skip-build    Skip building backend and frontend"
            echo "  --skip-deploy   Skip Docker deployment"
            echo "  -h, --help      Show this help message"
            echo ""
            echo "Examples:"
            echo "  $0                    # Full build and deploy"
            echo "  $0 --skip-build       # Deploy without building"
            echo "  $0 --skip-deploy      # Build only"
            exit 0
            ;;
        *)
            echo "Unknown option $1"
            echo "Use -h or --help for usage information"
            exit 1
            ;;
    esac
done

# Function to print colored output
print_status() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_header() {
    echo -e "${BLUE}================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}================================${NC}"
}

# Check if we are in the correct directory
if [ ! -f "../backend/pom.xml" ] || [ ! -f "../frontend/package.json" ]; then
    print_error "Please run this script from the infra/manual-deploy/ directory"
    exit 1
fi

# Check if required files exist
if [ ! -f "$DOCKER_COMPOSE_FILE" ]; then
    print_error "Docker compose file not found: $DOCKER_COMPOSE_FILE"
    exit 1
fi

if [ ! -f "$ENV_FILE" ]; then
    print_error "Environment file not found: $ENV_FILE"
    print_warning "Create it based on ../env.prod.example"
    exit 1
fi

# Check if Docker is running
if ! docker info >/dev/null 2>&1; then
    print_error "Docker is not running! Please start Docker and try again."
    exit 1
fi

print_header "EasyShop Complete Build and Deploy for Oracle Server"
echo "Environment: Production"
echo "Docker Compose: $DOCKER_COMPOSE_FILE"
echo "Environment File: $ENV_FILE"
echo ""

# Load environment variables
source "$ENV_FILE"

# Step 1: Build Backend (Java Spring Boot)
if [ "$SKIP_BUILD" = false ]; then
    print_header "Building Backend (Java Spring Boot)"
    
    print_status "Building backend with Maven..."
    cd ../backend
    
    # Clean and compile all modules
    mvn clean compile -q
    
    # Package all services
    print_status "Packaging microservices..."
    mvn package -DskipTests -q
    
    if [ $? -eq 0 ]; then
        print_status "Backend build completed successfully!"
    else
        print_error "Backend build failed!"
        exit 1
    fi
    
    cd ..
    echo ""
fi

# Step 2: Build Frontend (React + Vite)
if [ "$SKIP_BUILD" = false ]; then
    print_header "Building Frontend (React + Vite)"
    
    print_status "Installing frontend dependencies..."
    cd ../frontend
    npm install --silent
    
    print_status "Building frontend for production..."
    npm run build
    
    if [ $? -eq 0 ]; then
        print_status "Frontend build completed successfully!"
    else
        print_error "Frontend build failed!"
        exit 1
    fi
    
    cd ..
    echo ""
fi

# Step 3: Build Docker Images
if [ "$SKIP_BUILD" = false ]; then
    print_header "Building Docker Images"
    
    # Set default values if not provided
    DOCKER_REGISTRY=${DOCKER_REGISTRY:-"localhost"}
    IMAGE_VERSION=${IMAGE_VERSION:-"latest"}
    
    print_status "Docker Registry: $DOCKER_REGISTRY"
    print_status "Image Version: $IMAGE_VERSION"
    echo ""
    
    # Build all microservices
    print_status "Building auth-service..."
    docker build -t $DOCKER_REGISTRY/auth-service:$IMAGE_VERSION -f ../backend/auth-service/Dockerfile ../backend/
    
    print_status "Building product-service..."
    docker build -t $DOCKER_REGISTRY/product-service:$IMAGE_VERSION -f ../backend/product-service/Dockerfile ../backend/
    
    print_status "Building purchase-service..."
    docker build -t $DOCKER_REGISTRY/purchase-service:$IMAGE_VERSION -f ../backend/purchase-service/Dockerfile ../backend/
    
    print_status "Building api-gateway..."
    docker build -t $DOCKER_REGISTRY/api-gateway:$IMAGE_VERSION -f ../backend/api-gateway/Dockerfile ../backend/
    
    print_status "Building frontend..."
    docker build -t $DOCKER_REGISTRY/frontend:$IMAGE_VERSION -f ../frontend/Dockerfile ../frontend/
    
    print_status "All Docker images built successfully!"
    echo ""
fi

# Step 4: Deploy with Docker Compose
if [ "$SKIP_DEPLOY" = false ]; then
    print_header "Deploying with Docker Compose"
    
    # Stop existing containers
    print_status "Stopping existing containers..."
    docker compose -f "$DOCKER_COMPOSE_FILE" down --remove-orphans 2>/dev/null || true
    
    # Start services
    print_status "Starting services with Docker Compose..."
    docker compose -f "$DOCKER_COMPOSE_FILE" --env-file "$ENV_FILE" up -d
    
    if [ $? -eq 0 ]; then
        print_status "Services started successfully!"
    else
        print_error "Failed to start services!"
        exit 1
    fi
    
    echo ""
fi

# Step 5: Show deployment status
print_header "Deployment Status"

print_status "Checking service health..."
sleep 10

# Check if services are running
services=("db" "auth-service" "product-service" "purchase-service" "api-gateway" "frontend")
all_healthy=true

for service in "${services[@]}"; do
    if docker compose -f "$DOCKER_COMPOSE_FILE" ps --format "table {{.Name}}\t{{.Status}}" | grep -q "$service.*Up"; then
        print_status "✓ $service is running"
    else
        print_error "✗ $service is not running"
        all_healthy=false
    fi
done

echo ""

if [ "$all_healthy" = true ]; then
    print_header "Deployment Completed Successfully!"
    echo ""
    print_status "Your EasyShop application is now running:"
    echo "  Frontend:     http://$(hostname -I | awk '{print $1}')"
    echo "  API Gateway:  http://$(hostname -I | awk '{print $1}'):8080"
    echo "  Auth Service: http://$(hostname -I | awk '{print $1}'):9001"
    echo "  Product API:  http://$(hostname -I | awk '{print $1}'):9002"
    echo "  Purchase API: http://$(hostname -I | awk '{print $1}'):9003"
    echo ""
    print_status "Useful commands:"
    echo "  View logs:    docker compose -f $DOCKER_COMPOSE_FILE logs -f"
    echo "  Stop all:     docker compose -f $DOCKER_COMPOSE_FILE down"
    echo "  Restart:      docker compose -f $DOCKER_COMPOSE_FILE restart"
    echo "  Status:       docker compose -f $DOCKER_COMPOSE_FILE ps"
    echo ""
else
    print_error "Some services failed to start. Check logs with:"
    echo "  docker compose -f $DOCKER_COMPOSE_FILE logs"
    exit 1
fi
