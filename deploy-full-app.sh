#!/bin/bash
# Deploy full app (backend + frontend) to server

set -e

echo "=========================================="
echo "Deploying Clear Chain Full App"
echo "=========================================="
echo ""

# Check if we're on the server
if [ ! -d "/opt/myapp" ]; then
    echo "This script should be run on the server (in /opt/myapp)"
    exit 1
fi

cd /opt/myapp

echo "1. Pulling latest code..."
git pull || echo "Git pull failed or not a git repo, continuing..."

echo ""
echo "2. Building backend..."
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
./gradlew clean build -x test

echo ""
echo "3. Building and starting Docker containers..."
docker compose down
docker compose up -d --build

echo ""
echo "4. Waiting for services to start..."
sleep 10

echo ""
echo "5. Checking service status..."
docker compose ps

echo ""
echo "6. Updating Nginx configuration..."
sudo cp nginx-app.conf /etc/nginx/conf.d/app.conf
sudo nginx -t && sudo systemctl reload nginx

echo ""
echo "7. Updating landing page (clearchain.space)..."
if [ -d "deployment/landing" ]; then
  sudo cp -r deployment/landing/* /var/www/landing/ 2>/dev/null || true
  sudo chown -R root:root /var/www/landing 2>/dev/null || true
  sudo chmod -R 755 /var/www/landing 2>/dev/null || true
  echo "   Landing page files (index.html, logo.svg, images/) copied to /var/www/landing"
else
  echo "   Skipped (deployment/landing not found)"
fi

echo ""
echo "=========================================="
echo "Deployment complete!"
echo "=========================================="
echo ""
echo "Backend: http://localhost:8080"
echo "Frontend: http://localhost:3000"
echo "Public: https://app.clearchain.space"
echo ""
echo "Check logs with: docker compose logs -f"
