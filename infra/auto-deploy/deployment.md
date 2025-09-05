# EasyShop Deployment Guide

This guide will help you configure automatic deployment of EasyShop application on Oracle Cloud.

## CI/CD Architecture

```
GitHub Repository → GitHub Actions → Oracle Cloud Server
     ↓                    ↓                    ↓
   Code Push → Build & Test → Deploy to Production
```

## Prerequisites

### 1. Oracle Cloud Server
- Create a virtual machine in Oracle Cloud
- Recommended configuration: 2 vCPU, 4GB RAM, 50GB storage
- Operating system: Ubuntu 20.04/22.04, Oracle Linux 8 or CentOS 8
- Open ports: 22, 80, 8080, 9001-9003, 5432

### 2. GitHub Repository
- Make sure your code is in GitHub repository
- You have rights to configure GitHub Actions

## Oracle Cloud Server Setup

### Step 1: Connect to Server
```bash
# For Ubuntu
ssh ubuntu@your-oracle-server-ip

# For Oracle Linux
ssh opc@your-oracle-server-ip
```

### Step 2: Run Setup Script
```bash
# For Ubuntu
curl -O https://raw.githubusercontent.com/your-username/EasyShop/main/infra/setup-oracle-server.sh
chmod +x setup-oracle-server.sh
./setup-oracle-server.sh

# For Oracle Linux/CentOS
curl -O https://raw.githubusercontent.com/your-username/EasyShop/main/infra/setup-oracle-server-centos.sh
chmod +x setup-oracle-server-centos.sh
./setup-oracle-server-centos.sh
```

### Step 3: Clone Repository
```bash
cd /opt/easyshop
git clone https://github.com/your-username/EasyShop.git .
```

## GitHub Secrets Configuration

Go to your GitHub repository settings: `Settings → Secrets and variables → Actions`

Add the following secrets:

### Database Secrets
- `DB_USER` - database username (e.g.: `easyshop_user`)
- `DB_PASSWORD` - database password (use complex password)

### JWT Secrets
- `JWT_SECRET` - JWT secret key (minimum 32 characters)
- `JWT_TTL_MINUTES` - token lifetime in minutes (default: 60)

### Oracle Cloud Secrets
- `ORACLE_HOST` - your Oracle Cloud server IP address
- `ORACLE_USERNAME` - username (`ubuntu` for Ubuntu, `opc` for Oracle Linux)
- `ORACLE_SSH_KEY` - private SSH key for server connection
- `ORACLE_PORT` - SSH port (default: 22)
- `ORACLE_APP_PATH` - application path on server (default: `/opt/easyshop`)

### Docker Registry Secrets
- `DOCKER_USERNAME` - your GitHub username
- `DOCKER_PASSWORD` - GitHub Personal Access Token with packages rights

## File Structure

```
.github/workflows/
├── backend-tests.yml          # Backend testing
├── build-images.yml           # Docker images build
├── deploy-oracle.yml          # Oracle Cloud deployment
├── publish-auth-service.yml   # Auth service publication
├── publish-product-service.yml # Product service publication
├── publish-purchase-service.yml # Purchase service publication
└── publish-api-gateway.yml    # API Gateway publication

infra/
├── docker-compose-development.yml     # Development environment
├── docker-compose-production.yml      # Production environment
├── setup-oracle-server.sh             # Server setup script
└── environment-production.example     # Production variables example
```

## Deployment Process

### Automatic Deployment
1. **Push to main/master branch** → automatically triggers deployment
2. **GitHub Actions performs:**
   - Run tests
   - Build Docker images
   - Publish to GitHub Container Registry
   - Deploy to Oracle Cloud server

### Manual Deployment
1. Go to `Actions` in your GitHub repository
2. Select `Deploy to Oracle Cloud` workflow
3. Click `Run workflow`

## Docker Images

All images are published to GitHub Container Registry:
- `ghcr.io/your-username/easyshop/auth-service:latest`
- `ghcr.io/your-username/easyshop/product-service:latest`
- `ghcr.io/your-username/easyshop/purchase-service:latest`
- `ghcr.io/your-username/easyshop/api-gateway:latest`
- `ghcr.io/your-username/easyshop/frontend:latest`

## Application Access

After successful deployment, the application will be available at:
- **Frontend**: `http://your-oracle-server-ip`
- **API Gateway**: `http://your-oracle-server-ip:8080`
- **Auth Service**: `http://your-oracle-server-ip:9001`
- **Product Service**: `http://your-oracle-server-ip:9002`
- **Purchase Service**: `http://your-oracle-server-ip:9003`

## Application Management

### Check Status
```bash
cd /opt/easyshop
docker compose -f infra/docker-compose-production.yml ps
```

### View Logs
```bash
# All services
docker compose -f infra/docker-compose-production.yml logs

# Specific service
docker compose -f infra/docker-compose-production.yml logs auth-service
```

### Restart Application
```bash
cd /opt/easyshop
docker compose -f infra/docker-compose-production.yml restart
```

### Stop Application
```bash
cd /opt/easyshop
docker compose -f infra/docker-compose-production.yml down
```

## Troubleshooting

### Problem: SSH connection not working
- Check that SSH key is properly added to GitHub Secrets
- Make sure port 22 is open in Oracle Cloud firewall

### Problem: Docker images not loading
- Check that GitHub Token has packages rights
- Make sure DOCKER_USERNAME and DOCKER_PASSWORD are configured correctly

### Problem: Application not starting
- Check logs: `docker compose -f infra/docker-compose-production.yml logs`
- Make sure all environment variables are configured correctly
- Check that database is accessible

### Problem: Ports not open
- Make sure firewall is configured correctly
- Check Security Lists in Oracle Cloud Console

## Monitoring

### Health Checks
- **Database**: `pg_isready -U $DB_USER -d easyshop`
- **Services**: HTTP endpoints available on respective ports

### Logs
All logs are saved in Docker and available via:
```bash
docker compose -f infra/docker-compose-production.yml logs -f
```

## Application Update

To update the application, simply push to main branch:
```bash
git add .
git commit -m "Update application"
git push origin main
```

GitHub Actions will automatically:
1. Run tests
2. Build new images
3. Deploy update to server

## Additional Settings

### SSL/HTTPS
For production, it's recommended to configure SSL certificate via Let's Encrypt or Oracle Cloud Load Balancer.

### Backup
Configure automatic database backup:
```bash
# Create cron job for daily backup
0 2 * * * docker exec infra-db-1 pg_dump -U $DB_USER easyshop > /opt/backups/easyshop_$(date +\%Y\%m\%d).sql
```

### Monitoring
It's recommended to configure monitoring with Prometheus + Grafana or use Oracle Cloud Monitoring.