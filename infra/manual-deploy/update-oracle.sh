#!/bin/bash

# EasyShop Oracle Server Update Script
# This script safely updates the EasyShop application on Oracle server

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PROJECT_DIR="/opt/easyshop"
BACKUP_DIR="/opt/easyshop-backup-$(date +%Y%m%d-%H%M%S)"
DOCKER_COMPOSE_FILE="docker-compose.prod.yml"
ENV_FILE=".env.prod"

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
    echo "  EasyShop Oracle Server Update"
    echo "=========================================="
    echo -e "${NC}"
}

# Check if running as root
check_root() {
    if [[ $EUID -eq 0 ]]; then
        print_error "This script should not be run as root for security reasons"
        print_info "Please run as a regular user with sudo privileges"
        exit 1
    fi
}

# Check if Docker is running
check_docker() {
    if ! docker info >/dev/null 2>&1; then
        print_error "Docker is not running. Please start Docker first."
        exit 1
    fi
    print_success "Docker is running"
}

# Check if project directory exists
check_project_dir() {
    if [ ! -d "$PROJECT_DIR" ]; then
        print_error "Project directory $PROJECT_DIR does not exist"
        print_info "Please ensure the project is properly deployed first"
        exit 1
    fi
    print_success "Project directory found: $PROJECT_DIR"
}

# Create backup
create_backup() {
    print_info "Creating backup of current deployment..."
    
    if [ -d "$PROJECT_DIR" ]; then
        sudo cp -r "$PROJECT_DIR" "$BACKUP_DIR"
        print_success "Backup created: $BACKUP_DIR"
    else
        print_warning "No existing project directory to backup"
    fi
}

# Stop and remove old containers
stop_old_containers() {
    print_info "Stopping and removing old containers..."
    
    cd "$PROJECT_DIR" || exit 1
    
    # Stop containers if docker-compose file exists
    if [ -f "$DOCKER_COMPOSE_FILE" ]; then
        print_info "Stopping containers with docker-compose..."
        docker-compose -f "$DOCKER_COMPOSE_FILE" down --remove-orphans || true
    fi
    
    # Remove any remaining EasyShop containers
    print_info "Removing any remaining EasyShop containers..."
    docker ps -a --filter "name=easyshop" --format "{{.Names}}" | xargs -r docker rm -f || true
    
    # Remove EasyShop images
    print_info "Removing old EasyShop images..."
    docker images --filter "reference=*easyshop*" --format "{{.Repository}}:{{.Tag}}" | xargs -r docker rmi -f || true
    
    print_success "Old containers and images removed"
}

# Clean up old files
cleanup_old_files() {
    print_info "Cleaning up old project files..."
    
    cd "$PROJECT_DIR" || exit 1
    
    # Remove old project files but keep environment file
    if [ -f "$ENV_FILE" ]; then
        print_info "Backing up environment file..."
        sudo cp "$ENV_FILE" "/tmp/easyshop-env-backup"
    fi
    
    # Remove everything except .git directory
    sudo find . -maxdepth 1 -not -name '.git' -not -name '.' -exec rm -rf {} + || true
    
    print_success "Old files cleaned up"
}

# Pull latest changes from GitHub
pull_latest_changes() {
    print_info "Pulling latest changes from GitHub..."
    
    cd "$PROJECT_DIR" || exit 1
    
    # Check if git repository exists
    if [ ! -d ".git" ]; then
        print_error "Git repository not found in $PROJECT_DIR"
        print_info "Please ensure the project was cloned from GitHub"
        exit 1
    fi
    
    # Fetch latest changes
    print_info "Fetching latest changes..."
    git fetch origin
    
    # Check current branch
    CURRENT_BRANCH=$(git branch --show-current)
    print_info "Current branch: $CURRENT_BRANCH"
    
    # Pull latest changes
    print_info "Pulling latest changes..."
    git pull origin "$CURRENT_BRANCH"
    
    print_success "Latest changes pulled successfully"
}

# Restore environment file
restore_environment() {
    print_info "Restoring environment configuration..."
    
    cd "$PROJECT_DIR" || exit 1
    
    # Restore environment file if backup exists
    if [ -f "/tmp/easyshop-env-backup" ]; then
        sudo cp "/tmp/easyshop-env-backup" "$ENV_FILE"
        sudo rm "/tmp/easyshop-env-backup"
        print_success "Environment file restored"
    else
        print_warning "No environment file backup found"
        print_info "You may need to configure environment variables manually"
    fi
}

# Build and deploy new version
deploy_new_version() {
    print_info "Building and deploying new version..."
    
    cd "$PROJECT_DIR" || exit 1
    
    # Check if deployment script exists
    if [ -f "infra/manual-deploy/deploy-oracle.sh" ]; then
        print_info "Running deployment script..."
        chmod +x infra/manual-deploy/deploy-oracle.sh
        ./infra/manual-deploy/deploy-oracle.sh
    else
        print_error "Deployment script not found"
        print_info "Please ensure the project structure is correct"
        exit 1
    fi
}

# Verify deployment
verify_deployment() {
    print_info "Verifying deployment..."
    
    # Wait for services to start
    print_info "Waiting for services to start..."
    sleep 30
    
    # Check if containers are running
    print_info "Checking container status..."
    docker ps --filter "name=easyshop" --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
    
    # Check if services are responding
    print_info "Checking service health..."
    
    # Check auth service
    if curl -f -s http://localhost:9001/healthz >/dev/null 2>&1; then
        print_success "Auth service is healthy"
    else
        print_warning "Auth service may not be ready yet"
    fi
    
    # Check API gateway
    if curl -f -s http://localhost:8080/healthz >/dev/null 2>&1; then
        print_success "API Gateway is healthy"
    else
        print_warning "API Gateway may not be ready yet"
    fi
    
    # Check frontend
    if curl -f -s http://localhost:80 >/dev/null 2>&1; then
        print_success "Frontend is accessible"
    else
        print_warning "Frontend may not be ready yet"
    fi
}

# Show deployment info
show_deployment_info() {
    print_success "Deployment completed successfully!"
    echo ""
    print_info "Application URLs:"
    echo "  Frontend: http://$(curl -s ifconfig.me || echo 'your-server-ip')"
    echo "  API Gateway: http://$(curl -s ifconfig.me || echo 'your-server-ip'):8080"
    echo "  Auth Service: http://$(curl -s ifconfig.me || echo 'your-server-ip'):9001"
    echo ""
    print_info "Useful commands:"
    echo "  View logs: docker-compose -f $DOCKER_COMPOSE_FILE logs -f"
    echo "  Stop services: docker-compose -f $DOCKER_COMPOSE_FILE down"
    echo "  Restart services: docker-compose -f $DOCKER_COMPOSE_FILE restart"
    echo "  Check status: docker-compose -f $DOCKER_COMPOSE_FILE ps"
    echo ""
    print_info "Backup location: $BACKUP_DIR"
    print_warning "You can remove the backup after verifying the new deployment works correctly"
}

# Main execution
main() {
    print_header
    
    print_info "Starting EasyShop update process..."
    print_info "This will:"
    print_info "  1. Stop and remove old containers"
    print_info "  2. Pull latest changes from GitHub"
    print_info "  3. Deploy new version with enhanced security"
    print_info "  4. Verify deployment"
    echo ""
    
    # Confirm before proceeding
    read -p "Do you want to continue? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        print_info "Update cancelled by user"
        exit 0
    fi
    
    # Execute update steps
    check_root
    check_docker
    check_project_dir
    create_backup
    stop_old_containers
    cleanup_old_files
    pull_latest_changes
    restore_environment
    deploy_new_version
    verify_deployment
    show_deployment_info
    
    print_success "EasyShop update completed successfully! 🎉"
}

# Run main function
main "$@"
