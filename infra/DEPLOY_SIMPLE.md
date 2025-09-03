# EasyShop Simple Deployment Guide

This guide will help you quickly deploy EasyShop to your server.

## Prerequisites

1. **Server with Docker and Docker Compose**
2. **SSH access to the server**
3. **Local machine with Docker**

## Quick Start

### Step 1: Prepare Configuration

1. Copy the configuration example file:
   ```bash
   cp env.prod.example .env.prod
   ```

2. Edit `.env.prod` with your values:
   ```bash
   # Replace these values with yours
   DB_PASSWORD=your_secure_password_here
   JWT_SECRET=your_jwt_secret_key_here_minimum_32_characters
   ORACLE_HOST=your-oracle-server-ip
   ORACLE_USERNAME=ubuntu
   ```

### Step 2: Build Images

```bash
# Make script executable
chmod +x build-and-push.sh

# Build and publish images
./build-and-push.sh
```

### Step 3: Deploy to Server

```bash
# Make script executable
chmod +x deploy.sh

# Deploy the application
./deploy.sh
```

## Manual Deployment

If automatic scripts don't work, perform the steps manually:

### 1. Server Preparation

```bash
# Connect to server
ssh ubuntu@your-server-ip

# Install Docker (if not installed)
sudo apt update
sudo apt install -y docker.io docker-compose
sudo usermod -aG docker $USER
sudo systemctl enable docker
sudo systemctl start docker

# Create application directory
sudo mkdir -p /opt/easyshop
sudo chown $USER:$USER /opt/easyshop
```

### 2. Copy Files

```bash
# On local machine
scp docker-compose.prod.yml ubuntu@your-server-ip:/opt/easyshop/
scp .env.prod ubuntu@your-server-ip:/opt/easyshop/.env
```

### 3. Start Application

```bash
# On server
cd /opt/easyshop
docker compose -f docker-compose.prod.yml pull
docker compose -f docker-compose.prod.yml up -d
```

## Application Access

After successful deployment, your application will be available at:

- **Frontend**: `http://your-server-ip`
- **API Gateway**: `http://your-server-ip:8080`
- **Auth Service**: `http://your-server-ip:9001`
- **Product Service**: `http://your-server-ip:9002`
- **Purchase Service**: `http://your-server-ip:9003`

## Application Management

### View Logs
```bash
ssh ubuntu@your-server-ip 'cd /opt/easyshop && docker compose -f docker-compose.prod.yml logs -f'
```

### Stop Application
```bash
ssh ubuntu@your-server-ip 'cd /opt/easyshop && docker compose -f docker-compose.prod.yml down'
```

### Restart Application
```bash
ssh ubuntu@your-server-ip 'cd /opt/easyshop && docker compose -f docker-compose.prod.yml restart'
```

### Update Application
```bash
# On local machine
./build-and-push.sh

# On server
ssh ubuntu@your-server-ip 'cd /opt/easyshop && docker compose -f docker-compose.prod.yml pull && docker compose -f docker-compose.prod.yml up -d'
```

## Status Check

```bash
# Check container status
ssh ubuntu@your-server-ip 'cd /opt/easyshop && docker compose -f docker-compose.prod.yml ps'

# Check resource usage
ssh ubuntu@your-server-ip 'docker stats'
```

## Troubleshooting

### Problem: Cannot connect to server
- Check server IP address
- Make sure SSH key is added to `~/.ssh/authorized_keys`
- Check firewall settings

### Problem: Docker images not loading
- Check internet connection on server
- Make sure Docker registry is accessible
- Check registry access permissions

### Problem: Application not starting
- Check logs: `docker compose -f docker-compose.prod.yml logs`
- Make sure all environment variables are set correctly
- Check that ports are not occupied by other applications

## Useful Commands

```bash
# View all containers
docker ps -a

# View images
docker images

# Clean unused images
docker system prune -a

# View disk usage
docker system df
```

## Next Steps

1. **Configure domain** (optional)
2. **Configure SSL certificate** (optional)
3. **Configure monitoring** (optional)
4. **Configure backup** (optional)
