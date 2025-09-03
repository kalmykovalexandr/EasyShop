#!/bin/bash

# EasyShop Deployment Script
# This script will help you deploy the application to your server

set -e

echo "EasyShop Deployment Script"
echo "=========================="

# Check if we are in the correct directory
if [ ! -f "docker-compose.prod.yml" ]; then
    echo "Error: Run this script from the infra/ directory"
    exit 1
fi

# Check if .env.prod file exists
if [ ! -f ".env.prod" ]; then
    echo "Error: .env.prod file not found!"
    echo "Create .env.prod file based on env.prod.example"
    echo "   cp env.prod.example .env.prod"
    echo "   Then edit .env.prod with your values"
    exit 1
fi

# Load environment variables
source .env.prod

echo "Configuration:"
echo "   Host: $ORACLE_HOST"
echo "   User: $ORACLE_USERNAME"
echo "   App Path: $ORACLE_APP_PATH"
echo "   Image Version: $IMAGE_VERSION"
echo ""

# Check server connection
echo "Checking server connection..."
if ! ssh -o ConnectTimeout=10 -o BatchMode=yes $ORACLE_USERNAME@$ORACLE_HOST "echo 'Connection successful'" 2>/dev/null; then
    echo "Error: Cannot connect to server $ORACLE_HOST"
    echo "Make sure that:"
    echo "   1. SSH key is added to ~/.ssh/authorized_keys on the server"
    echo "   2. Server is accessible at IP $ORACLE_HOST"
    echo "   3. User $ORACLE_USERNAME exists"
    exit 1
fi

echo "Server connection successful!"

# Create directory on server
echo "Creating application directory on server..."
ssh $ORACLE_USERNAME@$ORACLE_HOST "sudo mkdir -p $ORACLE_APP_PATH && sudo chown $ORACLE_USERNAME:$ORACLE_USERNAME $ORACLE_APP_PATH"

# Copy files to server
echo "Copying files to server..."
scp docker-compose.prod.yml $ORACLE_USERNAME@$ORACLE_HOST:$ORACLE_APP_PATH/
scp .env.prod $ORACLE_USERNAME@$ORACLE_HOST:$ORACLE_APP_PATH/.env

# Start application on server
echo "Starting application on server..."
ssh $ORACLE_USERNAME@$ORACLE_HOST "cd $ORACLE_APP_PATH && docker compose -f docker-compose.prod.yml pull && docker compose -f docker-compose.prod.yml up -d"

echo ""
echo "Deployment completed!"
echo ""
echo "Your application is available at:"
echo "   Frontend: http://$ORACLE_HOST"
echo "   API Gateway: http://$ORACLE_HOST:8080"
echo ""
echo "To view logs use:"
echo "   ssh $ORACLE_USERNAME@$ORACLE_HOST 'cd $ORACLE_APP_PATH && docker compose -f docker-compose.prod.yml logs -f'"
echo ""
echo "To stop the application:"
echo "   ssh $ORACLE_USERNAME@$ORACLE_HOST 'cd $ORACLE_APP_PATH && docker compose -f docker-compose.prod.yml down'"
