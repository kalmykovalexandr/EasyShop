#!/bin/bash

# EasyShop Universal Deploy Script
# Handles all deployment scenarios:
# 1. First-time deployment (clean install)
# 2. Update existing deployment
# 3. Restart specific services
# 4. Full system restart
# Usage: ./deploy.sh [service1] [service2] ... [--force] [--clean]

set -e

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
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

success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

header() {
    echo -e "${BLUE}================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}================================${NC}"
}

# Parse arguments
FORCE=false
CLEAN=false
SERVICES=()

for arg in "$@"; do
    case $arg in
        --force)
            FORCE=true
            ;;
        --clean)
            CLEAN=true
            ;;
        --help|-h)
            echo "EasyShop Universal Deploy Script"
            echo ""
            echo "Usage: $0 [options] [services...]"
            echo ""
            echo "Options:"
            echo "  --force    Force deployment even if services are running"
            echo "  --clean    Clean all containers and images before deployment"
            echo "  --help     Show this help message"
            echo ""
            echo "Services:"
            echo "  auth-service     Authentication service"
            echo "  product-service  Product catalog service"
            echo "  purchase-service Purchase management service"
            echo "  api-gateway      API Gateway"
            echo "  frontend         Frontend application"
            echo "  db               Database (PostgreSQL)"
            echo ""
            echo "Examples:"
            echo "  $0                           # Deploy all services"
            echo "  $0 --clean                   # Clean install all services"
            echo "  $0 frontend                  # Deploy only frontend"
            echo "  $0 auth-service --force      # Force deploy auth service"
            echo "  $0 --clean --force           # Force clean install"
            exit 0
            ;;
        *)
            if [[ "$arg" =~ ^(config-server|auth-service|product-service|purchase-service|api-gateway|frontend|db)$ ]]; then
                SERVICES+=("$arg")
            else
                error "Unknown service: $arg. Use --help for available services."
            fi
            ;;
    esac
done

# If no services specified, deploy all
if [ ${#SERVICES[@]} -eq 0 ]; then
    SERVICES=("config-server" "auth-service" "product-service" "purchase-service" "api-gateway" "frontend" "db")
fi

# Check if we're in the right directory
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

print "Services to deploy: ${SERVICES[*]}"
print "Using Docker registry: ${DOCKER_REGISTRY:-local}"
print "Using image version: ${IMAGE_VERSION:-latest}"
print "Force mode: $FORCE"
print "Clean mode: $CLEAN"

# Function to check if port is in use
check_port() {
    local port=$1
    local service_name=$2
    
    if netstat -tuln 2>/dev/null | grep -q ":$port "; then
        if [ "$FORCE" = true ]; then
            warning "Port $port is in use, but --force is enabled. Will attempt to stop conflicting services."
            return 0
        else
            error "Port $port is already in use. Use --force to override or stop the conflicting service manually."
        fi
    fi
}

# Function to stop conflicting services
stop_conflicting_services() {
    print "Checking for conflicting services..."
    
    # Check for existing EasyShop containers
    local existing_containers=$(docker ps -a --format "table {{.Names}}" | grep -E "(easyshop|infra)" | grep -v "CONTAINER" || true)
    
    if [ -n "$existing_containers" ]; then
        warning "Found existing EasyShop containers:"
        echo "$existing_containers"
        
        if [ "$FORCE" = true ]; then
            print "Stopping existing containers..."
            echo "$existing_containers" | xargs -r docker stop
            print "Removing existing containers..."
            echo "$existing_containers" | xargs -r docker rm
        else
            error "Existing containers found. Use --force to stop them or stop manually first."
        fi
    fi
    
    # Check for port conflicts
    check_port 5432 "Database"
    check_port 80 "Frontend"
    check_port 8080 "API Gateway"
    check_port 9001 "Auth Service"
    check_port 9002 "Product Service"
    check_port 9003 "Purchase Service"
}

# Function to clean system
clean_system() {
    if [ "$CLEAN" = true ]; then
        header "Cleaning System"
        
        print "Stopping all EasyShop containers..."
        docker ps -a --format "table {{.Names}}" | grep -E "(easyshop|infra)" | grep -v "CONTAINER" | xargs -r docker stop
        
        print "Removing all EasyShop containers..."
        docker ps -a --format "table {{.Names}}" | grep -E "(easyshop|infra)" | grep -v "CONTAINER" | xargs -r docker rm
        
        print "Removing EasyShop images..."
        docker images --format "table {{.Repository}}:{{.Tag}}" | grep -E "(easyshop|localhost)" | xargs -r docker rmi -f
        
        print "Removing unused volumes..."
        docker volume prune -f
        
        print "Removing unused networks..."
        docker network prune -f
        
        success "System cleaned successfully"
    fi
}

# Function to build Docker images
build_images() {
    header "Building Docker Images"
    
    for service in "${SERVICES[@]}"; do
        case $service in
            "db")
                print "Skipping database build (using official PostgreSQL image)"
                ;;
            "config-server")
                print "Building config-server..."
                docker build -t ${DOCKER_REGISTRY:-easyshop}/config-server:${IMAGE_VERSION:-latest} -f ../backend/config-server/Dockerfile ../backend/
                ;;
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
        esac
        success "✓ $service built successfully"
    done
}

# Function to deploy services
deploy_services() {
    header "Deploying Services"
    
    print "Using Docker Compose configuration from docker-compose.yml"
    
    # Stop existing containers gracefully
    print "Stopping existing containers..."
    docker compose -f docker-compose.yml down 2>/dev/null || true
    
    # Start services
    print "Starting services..."
    docker compose -f docker-compose.yml up -d
    
    # Wait for services to be healthy
    print "Waiting for services to start..."
    sleep 10
    
    # Check service health
    print "Checking service health..."
    docker compose -f docker-compose.yml ps
    
    success "Deployment completed!"
}

# Function to show service status
show_status() {
    header "Service Status"
    
    print "Services available at:"
    print "  Frontend: http://localhost"
    print "  API Gateway: http://localhost:8080"
    print "  Auth Service: http://localhost:9001"
    print "  Product Service: http://localhost:9002"
    print "  Purchase Service: http://localhost:9003"
    print "  Database: localhost:5432"
    print ""
    print "Management commands:"
    print "  Check status: docker compose -f docker-compose.yml ps"
    print "  View logs: docker compose -f docker-compose.yml logs -f [service]"
    print "  Stop services: docker compose -f docker-compose.yml down"
    print "  Restart service: docker compose -f docker-compose.yml restart [service]"
}

# Main execution
main() {
    header "EasyShop Universal Deploy"
    
    # Clean system if requested
    clean_system
    
    # Stop conflicting services
    stop_conflicting_services
    
    # Build images
    build_images
    
    # Deploy services
    deploy_services
    
    # Show status
    show_status
}

# Run main function
main