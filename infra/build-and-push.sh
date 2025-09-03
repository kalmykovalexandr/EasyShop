#!/bin/bash

# EasyShop Build and Push Script
# This script builds and publishes Docker images

set -e

echo "EasyShop Build and Push Script"
echo "==============================="

# Check if we are in the correct directory
if [ ! -f "docker-compose.prod.yml" ]; then
    echo "Error: Run this script from the infra/ directory"
    exit 1
fi

# Check if .env.prod file exists
if [ ! -f ".env.prod" ]; then
    echo "Error: .env.prod file not found!"
    echo "Create .env.prod file based on env.prod.example"
    exit 1
fi

# Load environment variables
source .env.prod

echo "Configuration:"
echo "   Registry: $DOCKER_REGISTRY"
echo "   Version: $IMAGE_VERSION"
echo ""

# Check if Docker is running
if ! docker info >/dev/null 2>&1; then
    echo "Error: Docker is not running!"
    exit 1
fi

# Login to Docker registry (if needed)
echo "Logging into Docker registry..."
if [ "$DOCKER_REGISTRY" != "localhost" ]; then
    echo "If needed, run: docker login $DOCKER_REGISTRY"
fi

# Build images
echo "Building Docker images..."

# Auth Service
echo "   Building auth-service..."
docker build -t $DOCKER_REGISTRY/auth-service:$IMAGE_VERSION -f ../backend/auth-service/Dockerfile ../backend/

# Product Service
echo "   Building product-service..."
docker build -t $DOCKER_REGISTRY/product-service:$IMAGE_VERSION -f ../backend/product-service/Dockerfile ../backend/

# Purchase Service
echo "   Building purchase-service..."
docker build -t $DOCKER_REGISTRY/purchase-service:$IMAGE_VERSION -f ../backend/purchase-service/Dockerfile ../backend/

# API Gateway
echo "   Building api-gateway..."
docker build -t $DOCKER_REGISTRY/api-gateway:$IMAGE_VERSION -f ../backend/api-gateway/Dockerfile ../backend/

# Frontend
echo "   Building frontend..."
docker build -t $DOCKER_REGISTRY/frontend:$IMAGE_VERSION -f ../frontend/Dockerfile ../frontend/

echo "All images built successfully!"

# Push images (if not localhost)
if [ "$DOCKER_REGISTRY" != "localhost" ]; then
    echo "Pushing images to registry..."
    
    docker push $DOCKER_REGISTRY/auth-service:$IMAGE_VERSION
    docker push $DOCKER_REGISTRY/product-service:$IMAGE_VERSION
    docker push $DOCKER_REGISTRY/purchase-service:$IMAGE_VERSION
    docker push $DOCKER_REGISTRY/api-gateway:$IMAGE_VERSION
    docker push $DOCKER_REGISTRY/frontend:$IMAGE_VERSION
    
    echo "All images pushed successfully!"
else
    echo "Images built locally (localhost registry)"
fi

echo ""
echo "Build and push completed!"
echo ""
echo "Next steps:"
echo "   1. Run deploy.sh to deploy to server"
echo "   2. Or use docker-compose.prod.yml directly"
