# EasyShop Infrastructure & Deployment

This folder contains all infrastructure and deployment files for EasyShop project on Oracle Ubuntu server.

## Structure

```
infra/
├── manual-deploy/                    # Manual deployment (recommended)
│   └── deploy-oracle.sh              # Main deployment script
├── auto-deploy/                      # Automatic deployment
│   └── deployment.md                 # CI/CD documentation
├── docker-compose.dev.yml            # Development configuration
├── docker-compose.prod.yml           # Production configuration
├── env.prod.example                  # Environment variables example
└── README.md                         # This documentation
```

## Quick Start (Manual Deployment)

### 1. Server Setup (run once)

```bash
# Connect to server
ssh ubuntu@your-oracle-server-ip

# Update system packages
sudo apt update && sudo apt upgrade -y

# Install Java 21
sudo apt install -y openjdk-21-jdk

# Install Maven
sudo apt install -y maven

# Install Node.js and npm
curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash -
sudo apt install -y nodejs

# Install Docker
sudo apt install -y docker.io docker-compose
sudo usermod -aG docker $USER
sudo systemctl enable docker
sudo systemctl start docker

# Configure firewall
sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow 8080/tcp
sudo ufw allow 9001/tcp
sudo ufw allow 9002/tcp
sudo ufw allow 9003/tcp
sudo ufw allow 5432/tcp
sudo ufw --force enable

# Reboot to apply group changes
sudo reboot
```

### 2. Environment Variables Setup

```bash
# Create environment variables file
cp ../env.prod.example ../.env.prod

# Edit with your settings
nano ../.env.prod
```

Example content of `.env.prod`:
```bash
# Database Configuration
DB_USER=easyshop_user
DB_PASSWORD=your_secure_password_here

# JWT Configuration
JWT_SECRET=your_jwt_secret_key_here_minimum_32_characters
JWT_TTL_MINUTES=60

# Docker Configuration
IMAGE_VERSION=latest
DOCKER_REGISTRY=localhost
```

### 3. Application Deployment

```bash
# Go to deployment files folder
cd infra/manual-deploy

# Make script executable
chmod +x deploy-oracle.sh

# Run full deployment
./deploy-oracle.sh
```

## What the deployment script does

1. **Backend Build** - compiles and packages all Java microservices with Maven
2. **Frontend Build** - builds React application for production with npm
3. **Docker Images Creation** - builds images for all services
4. **Deployment** - starts all services via Docker Compose
5. **Health Check** - verifies that all services are running

## Additional Options

```bash
# Deployment only (without build)
./deploy-oracle.sh --skip-build

# Build only (without deployment)
./deploy-oracle.sh --skip-deploy

# Show help
./deploy-oracle.sh --help
```

## Result

After successful execution, the application will be available at:

- **Frontend**: http://your-server-ip
- **API Gateway**: http://your-server-ip:8080
- **Auth Service**: http://your-server-ip:9001
- **Product API**: http://your-server-ip:9002
- **Purchase API**: http://your-server-ip:9003

## Application Management

```bash
# View logs
docker compose -f ../docker-compose.prod.yml logs -f

# Stop all services
docker compose -f ../docker-compose.prod.yml down

# Restart services
docker compose -f ../docker-compose.prod.yml restart

# Service status
docker compose -f ../docker-compose.prod.yml ps
```

## Troubleshooting

### Error "Docker is not running"
```bash
sudo systemctl start docker
sudo systemctl enable docker
```

### Error "Backend build failed"
```bash
# Check Java
java -version

# Check Maven
mvn --version

# Build manually
cd backend && mvn clean package
```

### Error "Frontend build failed"
```bash
# Check Node.js
node --version
npm --version

# Build manually
cd frontend && npm install && npm run build
```

### Services not starting
```bash
# Check logs
docker compose -f ../docker-compose.prod.yml logs

# Check environment variables
cat ../.env.prod

# Check ports
sudo netstat -tlnp | grep -E ':(80|8080|9001|9002|9003|5432)'
```

## Application Update

### Quick Update (Recommended)
```bash
# Run the update script
./update-oracle.sh
```

### Manual Update
```bash
# Stop current version
docker compose -f ../docker-compose.prod.yml down

# Pull latest changes
git pull origin main

# Start new version
./deploy-oracle.sh
```

### Complete Clean Install
```bash
# Clean everything
./clean-oracle.sh

# Clone fresh
git clone <your-repo-url> /opt/easyshop
cd /opt/easyshop

# Deploy fresh
cd infra/manual-deploy
./deploy-oracle.sh
```

## Cleanup

```bash
# Remove unused Docker images
docker system prune -a

# Remove all EasyShop containers and images
docker compose -f ../docker-compose.prod.yml down --rmi all
```

## Technical Requirements

### Server
- **OS**: Ubuntu 20.04/22.04
- **RAM**: minimum 4GB
- **CPU**: minimum 2 vCPU
- **Disk**: minimum 50GB

### Software
- **Java 21+** (installed by setup script)
- **Maven** (installed by setup script)
- **Node.js 20+** (installed by setup script)
- **Docker & Docker Compose** (installed by setup script)

## Security

- Configure firewall to open only necessary ports
- Use complex passwords for database
- Configure JWT secrets with sufficient length (minimum 32 characters)
- Regularly update dependencies

## Monitoring

- Check logs: `docker compose -f ../docker-compose.prod.yml logs`
- Monitor resource usage: `docker stats`
- Configure health checks for critical services

## Automatic Deployment

If you want to configure automatic deployment via GitHub Actions, use files in `auto-deploy/` folder. Follow instructions in `auto-deploy/deployment.md`.

## Support

If you encounter problems:
1. Check service logs
2. Make sure all environment variables are configured correctly
3. Check that all ports are open in firewall
4. Make sure server has sufficient resources