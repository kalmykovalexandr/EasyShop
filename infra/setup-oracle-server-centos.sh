#!/bin/bash

# Oracle Cloud Server Setup Script (Oracle Linux/CentOS)
# Run this script on your Oracle Cloud instance to prepare it for EasyShop deployment

set -e

echo "🚀 Setting up Oracle Cloud server for EasyShop deployment..."

# Update system packages
echo "📦 Updating system packages..."
sudo yum update -y

# Install Docker
echo "🐳 Installing Docker..."
sudo yum install -y yum-utils
sudo yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
sudo yum install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
sudo systemctl start docker
sudo systemctl enable docker

# Add current user to docker group
sudo usermod -aG docker $USER

# Install Git
echo "📁 Installing Git..."
sudo yum install -y git

# Create application directory
echo "📂 Creating application directory..."
sudo mkdir -p /opt/easyshop
sudo chown $USER:$USER /opt/easyshop

# Clone repository (you'll need to do this manually or provide access)
echo "📥 Repository setup..."
echo "Please clone your repository to /opt/easyshop:"
echo "git clone https://github.com/your-username/EasyShop.git /opt/easyshop"

# Configure firewall
echo "🔥 Configuring firewall..."
sudo firewall-cmd --permanent --add-port=22/tcp
sudo firewall-cmd --permanent --add-port=80/tcp
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --permanent --add-port=9001/tcp
sudo firewall-cmd --permanent --add-port=9002/tcp
sudo firewall-cmd --permanent --add-port=9003/tcp
sudo firewall-cmd --permanent --add-port=5432/tcp
sudo firewall-cmd --reload

# Create systemd service for EasyShop
echo "⚙️ Creating systemd service..."
sudo tee /etc/systemd/system/easyshop.service > /dev/null <<EOF
[Unit]
Description=EasyShop Application
Requires=docker.service
After=docker.service

[Service]
Type=oneshot
RemainAfterExit=yes
WorkingDirectory=/opt/easyshop
ExecStart=/usr/bin/docker compose -f infra/docker-compose.prod.yml up -d
ExecStop=/usr/bin/docker compose -f infra/docker-compose.prod.yml down
TimeoutStartSec=0

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable easyshop.service

echo "✅ Oracle Cloud server setup completed!"
echo ""
echo "Next steps:"
echo "1. Clone your repository: git clone https://github.com/your-username/EasyShop.git /opt/easyshop"
echo "2. Create .env.prod file with your production variables"
echo "3. Configure GitHub secrets for deployment"
echo "4. Push to main branch to trigger deployment"
echo ""
echo "To start the application manually:"
echo "cd /opt/easyshop && docker compose -f infra/docker-compose.prod.yml up -d"
echo ""
echo "To check application status:"
echo "docker compose -f infra/docker-compose.prod.yml ps"
