#!/bin/bash

# EasyShop Oracle Server Status Check Script
# This script checks the status of EasyShop on Oracle server

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PROJECT_DIR="/opt/easyshop"
DOCKER_COMPOSE_FILE="docker-compose.prod.yml"

# Functions
print_info() {
    echo -e "${BLUE}ℹ️  $1${NC}"
}

print_success() {
    echo -e "${GREEN}✅ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠️  $1${NC}"
}

print_error() {
    echo -e "${RED}❌ $1${NC}"
}

print_header() {
    echo -e "${BLUE}"
    echo "=========================================="
    echo "  EasyShop Oracle Server Status Check"
    echo "=========================================="
    echo -e "${NC}"
}

# Check Docker status
check_docker() {
    print_info "Checking Docker status..."
    
    if docker info >/dev/null 2>&1; then
        print_success "Docker is running"
    else
        print_error "Docker is not running"
        return 1
    fi
}

# Check project directory
check_project_dir() {
    print_info "Checking project directory..."
    
    if [ -d "$PROJECT_DIR" ]; then
        print_success "Project directory exists: $PROJECT_DIR"
    else
        print_error "Project directory not found: $PROJECT_DIR"
        return 1
    fi
}

# Check Docker Compose file
check_docker_compose() {
    print_info "Checking Docker Compose configuration..."
    
    if [ -f "$PROJECT_DIR/$DOCKER_COMPOSE_FILE" ]; then
        print_success "Docker Compose file found"
    else
        print_error "Docker Compose file not found: $DOCKER_COMPOSE_FILE"
        return 1
    fi
}

# Check running containers
check_containers() {
    print_info "Checking running containers..."
    
    cd "$PROJECT_DIR" || exit 1
    
    # Get container status
    CONTAINERS=$(docker-compose -f "$DOCKER_COMPOSE_FILE" ps --format "table {{.Name}}\t{{.Status}}\t{{.Ports}}")
    
    if [ -n "$CONTAINERS" ]; then
        echo "$CONTAINERS"
        
        # Count running containers
        RUNNING_COUNT=$(docker-compose -f "$DOCKER_COMPOSE_FILE" ps -q | wc -l)
        print_success "Found $RUNNING_COUNT containers"
    else
        print_warning "No containers found"
    fi
}

# Check service health
check_service_health() {
    print_info "Checking service health..."
    
    # Check Auth Service
    print_info "Checking Auth Service (port 9001)..."
    if curl -f -s http://localhost:9001/healthz >/dev/null 2>&1; then
        print_success "Auth Service is healthy"
    else
        print_error "Auth Service is not responding"
    fi
    
    # Check Product Service
    print_info "Checking Product Service (port 9002)..."
    if curl -f -s http://localhost:9002/healthz >/dev/null 2>&1; then
        print_success "Product Service is healthy"
    else
        print_error "Product Service is not responding"
    fi
    
    # Check Purchase Service
    print_info "Checking Purchase Service (port 9003)..."
    if curl -f -s http://localhost:9003/healthz >/dev/null 2>&1; then
        print_success "Purchase Service is healthy"
    else
        print_error "Purchase Service is not responding"
    fi
    
    # Check API Gateway
    print_info "Checking API Gateway (port 8080)..."
    if curl -f -s http://localhost:8080/healthz >/dev/null 2>&1; then
        print_success "API Gateway is healthy"
    else
        print_error "API Gateway is not responding"
    fi
    
    # Check Frontend
    print_info "Checking Frontend (port 80)..."
    if curl -f -s http://localhost >/dev/null 2>&1; then
        print_success "Frontend is accessible"
    else
        print_error "Frontend is not accessible"
    fi
}

# Check disk space
check_disk_space() {
    print_info "Checking disk space..."
    
    DISK_USAGE=$(df -h / | awk 'NR==2 {print $5}' | sed 's/%//')
    
    if [ "$DISK_USAGE" -lt 80 ]; then
        print_success "Disk usage: ${DISK_USAGE}% (OK)"
    elif [ "$DISK_USAGE" -lt 90 ]; then
        print_warning "Disk usage: ${DISK_USAGE}% (Warning)"
    else
        print_error "Disk usage: ${DISK_USAGE}% (Critical)"
    fi
}

# Check memory usage
check_memory() {
    print_info "Checking memory usage..."
    
    MEMORY_USAGE=$(free | awk 'NR==2{printf "%.0f", $3*100/$2}')
    
    if [ "$MEMORY_USAGE" -lt 80 ]; then
        print_success "Memory usage: ${MEMORY_USAGE}% (OK)"
    elif [ "$MEMORY_USAGE" -lt 90 ]; then
        print_warning "Memory usage: ${MEMORY_USAGE}% (Warning)"
    else
        print_error "Memory usage: ${MEMORY_USAGE}% (Critical)"
    fi
}

# Check Docker images
check_docker_images() {
    print_info "Checking Docker images..."
    
    IMAGES=$(docker images --filter "reference=*easyshop*" --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}\t{{.CreatedAt}}")
    
    if [ -n "$IMAGES" ]; then
        echo "$IMAGES"
        print_success "EasyShop images found"
    else
        print_warning "No EasyShop images found"
    fi
}

# Check recent logs
check_recent_logs() {
    print_info "Checking recent logs (last 10 lines)..."
    
    cd "$PROJECT_DIR" || exit 1
    
    if [ -f "$DOCKER_COMPOSE_FILE" ]; then
        echo "=== Recent logs ==="
        docker-compose -f "$DOCKER_COMPOSE_FILE" logs --tail=10
    else
        print_warning "Cannot check logs - Docker Compose file not found"
    fi
}

# Show application URLs
show_urls() {
    print_info "Application URLs:"
    
    SERVER_IP=$(curl -s ifconfig.me 2>/dev/null || echo "your-server-ip")
    
    echo "  Frontend: http://$SERVER_IP"
    echo "  API Gateway: http://$SERVER_IP:8080"
    echo "  Auth Service: http://$SERVER_IP:9001"
    echo "  Product Service: http://$SERVER_IP:9002"
    echo "  Purchase Service: http://$SERVER_IP:9003"
}

# Main execution
main() {
    print_header
    
    # Basic checks
    check_docker
    check_project_dir
    check_docker_compose
    
    echo ""
    
    # Container checks
    check_containers
    
    echo ""
    
    # Health checks
    check_service_health
    
    echo ""
    
    # System checks
    check_disk_space
    check_memory
    
    echo ""
    
    # Docker checks
    check_docker_images
    
    echo ""
    
    # Logs
    check_recent_logs
    
    echo ""
    
    # URLs
    show_urls
    
    echo ""
    print_success "Status check completed!"
}

# Run main function
main "$@"
