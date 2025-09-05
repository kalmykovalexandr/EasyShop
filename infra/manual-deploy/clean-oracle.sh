#!/bin/bash

# EasyShop Oracle Server Clean Script
# This script completely removes EasyShop from Oracle server

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
    echo "  EasyShop Oracle Server Clean"
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

# Create backup before cleanup
create_backup() {
    print_info "Creating backup before cleanup..."
    
    if [ -d "$PROJECT_DIR" ]; then
        sudo cp -r "$PROJECT_DIR" "$BACKUP_DIR"
        print_success "Backup created: $BACKUP_DIR"
    else
        print_warning "No project directory to backup"
    fi
}

# Stop and remove all containers
stop_all_containers() {
    print_info "Stopping all EasyShop containers..."
    
    # Stop all containers with EasyShop in the name
    docker ps -a --filter "name=easyshop" --format "{{.Names}}" | xargs -r docker stop || true
    docker ps -a --filter "name=easyshop" --format "{{.Names}}" | xargs -r docker rm -f || true
    
    # Also stop by image name patterns
    docker ps -a --filter "ancestor=*easyshop*" --format "{{.Names}}" | xargs -r docker stop || true
    docker ps -a --filter "ancestor=*easyshop*" --format "{{.Names}}" | xargs -r docker rm -f || true
    
    print_success "All EasyShop containers stopped and removed"
}

# Remove all EasyShop images
remove_all_images() {
    print_info "Removing all EasyShop images..."
    
    # Remove images by name pattern
    docker images --filter "reference=*easyshop*" --format "{{.Repository}}:{{.Tag}}" | xargs -r docker rmi -f || true
    
    # Remove images by label
    docker images --filter "label=easyshop" --format "{{.Repository}}:{{.Tag}}" | xargs -r docker rmi -f || true
    
    print_success "All EasyShop images removed"
}

# Remove all EasyShop volumes
remove_all_volumes() {
    print_info "Removing all EasyShop volumes..."
    
    # Remove volumes by name pattern
    docker volume ls --filter "name=easyshop" --format "{{.Name}}" | xargs -r docker volume rm -f || true
    
    print_success "All EasyShop volumes removed"
}

# Remove all EasyShop networks
remove_all_networks() {
    print_info "Removing all EasyShop networks..."
    
    # Remove networks by name pattern
    docker network ls --filter "name=easyshop" --format "{{.Name}}" | xargs -r docker network rm || true
    
    print_success "All EasyShop networks removed"
}

# Clean up project directory
cleanup_project_directory() {
    print_info "Cleaning up project directory..."
    
    if [ -d "$PROJECT_DIR" ]; then
        sudo rm -rf "$PROJECT_DIR"
        print_success "Project directory removed: $PROJECT_DIR"
    else
        print_warning "Project directory not found: $PROJECT_DIR"
    fi
}

# Clean up Docker system
cleanup_docker_system() {
    print_info "Cleaning up Docker system..."
    
    # Remove unused containers
    docker container prune -f
    
    # Remove unused images
    docker image prune -f
    
    # Remove unused volumes
    docker volume prune -f
    
    # Remove unused networks
    docker network prune -f
    
    print_success "Docker system cleaned up"
}

# Show cleanup summary
show_cleanup_summary() {
    print_success "EasyShop cleanup completed!"
    echo ""
    print_info "What was removed:"
    echo "  - All EasyShop containers"
    echo "  - All EasyShop images"
    echo "  - All EasyShop volumes"
    echo "  - All EasyShop networks"
    echo "  - Project directory: $PROJECT_DIR"
    echo "  - Unused Docker resources"
    echo ""
    print_info "Backup location: $BACKUP_DIR"
    print_warning "You can remove the backup if you're sure you don't need it"
    echo ""
    print_info "To deploy fresh:"
    echo "  1. Clone the repository: git clone <your-repo-url> $PROJECT_DIR"
    echo "  2. Run deployment: ./infra/manual-deploy/deploy-oracle.sh"
}

# Main execution
main() {
    print_header
    
    print_warning "This will completely remove EasyShop from the server!"
    print_info "This includes:"
    print_info "  - All containers, images, volumes, and networks"
    print_info "  - Project directory and files"
    print_info "  - Docker system cleanup"
    echo ""
    print_warning "A backup will be created before cleanup"
    echo ""
    
    # Confirm before proceeding
    read -p "Are you sure you want to continue? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        print_info "Cleanup cancelled by user"
        exit 0
    fi
    
    # Execute cleanup steps
    check_root
    create_backup
    stop_all_containers
    remove_all_images
    remove_all_volumes
    remove_all_networks
    cleanup_project_directory
    cleanup_docker_system
    show_cleanup_summary
    
    print_success "EasyShop completely removed from server! 🧹"
}

# Run main function
main "$@"
