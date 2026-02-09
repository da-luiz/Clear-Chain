# DigitalOcean Deployment Guide - Complete Specification

This document provides complete deployment instructions for **DigitalOcean Droplet + Docker + Cloudflare + Azure Entra ID**.

## Overview

**Architecture:**
```
Browser → Cloudflare (proxy, WAF, TLS) → Nginx (host) → Spring Boot (Docker)
                                      └─ Landing Page (static)
```

**Hostnames:**
- Landing page: `https://<domain>.com`
- Application: `https://app.<domain>.com`

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [DigitalOcean Droplet Setup](#digitalocean-droplet-setup)
3. [Docker & Docker Compose](#docker--docker-compose)
4. [Cloudflare Configuration](#cloudflare-configuration)
5. [Nginx Configuration](#nginx-configuration)
6. [Azure Entra ID Setup](#azure-entra-id-setup)
7. [Application Deployment](#application-deployment)
8. [Landing Page](#landing-page)
9. [Runbook](#runbook)

---

## Prerequisites

### Required Accounts

1. **DigitalOcean** - https://www.digitalocean.com/
2. **Cloudflare** - https://dash.cloudflare.com/sign-up
3. **Microsoft Azure** - https://azure.microsoft.com/free (for Azure Entra ID)
4. **Domain Name** - Registered domain for `<domain>.com`

### Required Access

- SSH key pair for server access
- Azure Entra ID admin access (to create app registration)
- Cloudflare account with domain added

### 0.1 Setup SSH Keys (IMPORTANT - Do This First!)

**Before creating the droplet, set up your SSH keys properly:**

1. **Check if you already have SSH keys:**
```bash
ls -la ~/.ssh/
```

2. **If you don't have keys, generate a new one:**
```bash
ssh-keygen -t ed25519 -C "your_email@example.com" -f ~/.ssh/digitalocean_key
# Press Enter to accept default location, or specify a custom name
# You can leave passphrase empty for easier access (or set one for security)
```

3. **Set correct permissions:**
```bash
chmod 600 ~/.ssh/digitalocean_key
chmod 644 ~/.ssh/digitalocean_key.pub
```

4. **Display your public key (you'll need this for DigitalOcean):**
```bash
cat ~/.ssh/digitalocean_key.pub
```

5. **Copy the entire output** - it should look like:
```
ssh-ed25519 AAAAC3NzaC1lZDI1NTE5AAAAI... your_email@example.com
```

6. **Add this public key to DigitalOcean:**
   - Go to DigitalOcean Dashboard → **Settings** → **Security** → **SSH Keys**
   - Click **Add SSH Key**
   - Paste your public key
   - Give it a name (e.g., "Fedora Laptop")
   - Click **Add SSH Key**

7. **Create SSH config for easy access (optional but recommended):**
```bash
cat >> ~/.ssh/config << 'EOF'
Host clear-chain-vps
    HostName YOUR_DROPLET_IP
    User root
    IdentityFile ~/.ssh/digitalocean_key
    IdentitiesOnly yes
    StrictHostKeyChecking no
EOF

chmod 600 ~/.ssh/config
```

**After creating the droplet, you can connect with:**
```bash
ssh clear-chain-vps
# Or if you didn't create the config:
ssh -i ~/.ssh/digitalocean_key root@YOUR_DROPLET_IP
```

---

## 1. DigitalOcean Droplet Setup

### 1.1 Create Droplet

1. Go to **DigitalOcean Dashboard** → **Create** → **Droplets**
2. Configure droplet:
   - **Image**: Fedora (latest version)
   - **Plan**: Basic plan, Regular Intel with SSD
   - **CPU**: 1 vCPU, 2GB RAM
   - **Disk**: 50GB SSD
   - **Transfer**: 2TB
   - **Datacenter region**: Choose nearest to your users
   - **Authentication**: SSH keys (add your public SSH key)
   - **Hostname**: `clear-chain-vps`
   - **Tags**: Optional (e.g., `production`, `web-app`)

3. Click **Create Droplet**
4. **Note the IP address** - You'll need this for Cloudflare DNS

### 1.2 Initial Server Setup

**Connect to server:**

If you created the SSH config (recommended):
```bash
ssh clear-chain-vps
```

Or if you didn't create the config:
```bash
ssh -i ~/.ssh/digitalocean_key root@<DROPLET_IP>
```

**If connection fails:**
- Make sure your public key was added to DigitalOcean before creating the droplet
- Verify key permissions: `chmod 600 ~/.ssh/digitalocean_key`
- Check that you selected the correct SSH key when creating the droplet

#### Create Deploy User
```bash
# Create user
useradd -m -s /bin/bash deploy

# Add to wheel group (Fedora's sudo group)
usermod -aG wheel deploy

# Copy SSH keys
mkdir -p /home/deploy/.ssh
cp ~/.ssh/authorized_keys /home/deploy/.ssh/
chown -R deploy:deploy /home/deploy/.ssh
chmod 700 /home/deploy/.ssh
chmod 600 /home/deploy/.ssh/authorized_keys
```

#### Disable Root SSH and Password Auth
```bash
# Edit SSH config
nano /etc/ssh/sshd_config

# Set these values:
PermitRootLogin no
PasswordAuthentication no
PubkeyAuthentication yes

# Restart SSH
systemctl restart sshd
```

#### Enable Automatic Security Updates
```bash
# Install dnf-automatic for automatic updates
dnf install -y dnf-automatic

# Configure automatic updates
sed -i 's/apply_updates = no/apply_updates = yes/' /etc/dnf/automatic.conf
sed -i 's/upgrade_type = default/upgrade_type = security/' /etc/dnf/automatic.conf

# Enable and start the timer
systemctl enable --now dnf-automatic.timer

# Verify
systemctl status dnf-automatic.timer
```

### 1.3 Configure Firewall (firewalld)

Fedora uses `firewalld` by default. Configure it:

```bash
# Start and enable firewalld
systemctl start firewalld
systemctl enable firewalld

# Allow SSH, HTTP, HTTPS
firewall-cmd --permanent --add-service=ssh
firewall-cmd --permanent --add-service=http
firewall-cmd --permanent --add-service=https

# Reload firewall to apply changes
firewall-cmd --reload

# Verify
firewall-cmd --list-all
```

**Alternative: Use UFW (if preferred)**
```bash
# Install UFW
dnf install -y ufw

# Default deny incoming
ufw default deny incoming
ufw default allow outgoing

# Allow SSH, HTTP, HTTPS
ufw allow 22/tcp
ufw allow 80/tcp
ufw allow 443/tcp

# Enable firewall
ufw --force enable

# Verify
ufw status
```

**Verification:**
- Only ports 22, 80, 443 should be allowed
- Try SSH with key - should work

### 1.4 Restrict HTTP/HTTPS to Cloudflare IPs Only (Advanced Security)

**⚠️ IMPORTANT: Do this AFTER your application is deployed and working!**

This step restricts HTTP (port 80) and HTTPS (port 443) traffic to only come from Cloudflare IP addresses. This prevents direct attacks on your server.

**For firewalld (Fedora default):**

```bash
# Download Cloudflare IP ranges
curl -s https://www.cloudflare.com/ips-v4 -o /tmp/cloudflare-ips-v4.txt

# Remove existing HTTP/HTTPS rules (keep SSH)
firewall-cmd --permanent --remove-service=http
firewall-cmd --permanent --remove-service=https

# Add Cloudflare IP ranges for HTTP/HTTPS
while read ip; do
  firewall-cmd --permanent --add-rich-rule="rule family='ipv4' source address='$ip' port port='80' protocol='tcp' accept"
  firewall-cmd --permanent --add-rich-rule="rule family='ipv4' source address='$ip' port port='443' protocol='tcp' accept"
done < /tmp/cloudflare-ips-v4.txt

# Reload firewall
firewall-cmd --reload

# Verify
firewall-cmd --list-all
```

**Alternative: Use a script to add all Cloudflare IPs at once:**

```bash
# Create script to add Cloudflare IPs
cat > /tmp/add-cloudflare-ips.sh << 'EOF'
#!/bin/bash
curl -s https://www.cloudflare.com/ips-v4 | while read ip; do
  firewall-cmd --permanent --add-rich-rule="rule family='ipv4' source address='$ip' port port='80' protocol='tcp' accept"
  firewall-cmd --permanent --add-rich-rule="rule family='ipv4' source address='$ip' port port='443' protocol='tcp' accept"
done
firewall-cmd --reload
EOF

chmod +x /tmp/add-cloudflare-ips.sh
/tmp/add-cloudflare-ips.sh
```

**Note:** SSH (port 22) remains open from anywhere for server management. Only HTTP/HTTPS are restricted to Cloudflare IPs.
- Try SSH with password - should fail

---

## 2. Docker & Docker Compose

### 2.1 Install Docker

```bash
# Update packages
dnf update -y

# Install required packages
dnf install -y dnf-plugins-core

# Add Docker repository
dnf config-manager --add-repo https://download.docker.com/linux/fedora/docker-ce.repo

# Install Docker
dnf install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# Start and enable Docker
systemctl start docker
systemctl enable docker

# Verify installation
docker --version
docker compose version
```

### 2.2 Add Deploy User to Docker Group

```bash
usermod -aG docker deploy

# Test (logout and login again)
su - deploy
docker ps
```

### 2.3 Create Application Directory

```bash
mkdir -p /opt/myapp
cd /opt/myapp
chown deploy:deploy /opt/myapp
```

**Directory structure:**
```
/opt/myapp/
├── docker-compose.yml
├── .env          # chmod 600
└── README-deploy.md
```

---

## 3. Cloudflare Configuration

### 3.1 Add Domain to Cloudflare

1. Go to **Cloudflare Dashboard**
2. **Add Site** → Enter your domain
3. Choose **Free** plan (sufficient for most use cases)
4. Cloudflare will scan your DNS records

### 3.2 Update Nameservers

1. Copy Cloudflare nameservers (shown in dashboard)
2. Go to your domain registrar
3. Update nameservers to Cloudflare's nameservers
4. Wait for propagation (can take up to 24 hours, usually < 1 hour)

### 3.3 Configure DNS (Proxied)

Add DNS records (with **orange cloud ON** - proxied):

```
Type  Name  Content                Proxy
A     @     <DROPLET_PUBLIC_IP>    ✅ (orange)
A     app   <DROPLET_PUBLIC_IP>    ✅ (orange)
```

**Important:** Proxy must be enabled (orange cloud) for Cloudflare to work.

### 3.4 Configure SSL/TLS

1. Go to **SSL/TLS** tab
2. Set **SSL/TLS encryption mode**: **Full (strict)**
   - In the SSL/TLS Overview page, click **Configure**
   - Select **Full (strict)** mode
   - This ensures encrypted connection between Cloudflare and your origin server
3. Enable **Always Use HTTPS**:
   - In the SSL/TLS tab, go to **Edge Certificates** (or scroll down to find it)
   - Find **Always Use HTTPS** toggle
   - Turn it **ON**
   - This automatically redirects all HTTP traffic to HTTPS

### 3.5 Create Origin Certificate

1. Go to **SSL/TLS** → **Origin Server**
2. Click **Create Certificate**
3. Select:
   - **Hostnames**: `<domain>.com`, `app.<domain>.com`
   - **Private key type**: RSA (2048)
   - **Certificate Validity**: 15 years (max)

4. **Copy both:**
   - **Origin Certificate** (PEM encoded)
   - **Private Key** (PEM encoded)

5. **Save to Droplet:**
```bash
# Create certificate directory (requires sudo)
sudo mkdir -p /etc/ssl/certs
sudo mkdir -p /etc/ssl/private

# Save certificate
sudo nano /etc/ssl/certs/cf-origin.pem
# Paste Origin Certificate content (from Cloudflare)
# Save and exit: Ctrl+X, then Y, then Enter

# Save private key
sudo nano /etc/ssl/private/cf-origin.key
# Paste Private Key content (from Cloudflare)
# Save and exit: Ctrl+X, then Y, then Enter

# Set permissions
# Certificate (public) - readable by all
sudo chmod 644 /etc/ssl/certs/cf-origin.pem
sudo chown root:root /etc/ssl/certs/cf-origin.pem

# Private key permissions
# If Nginx is already installed (creates nginx user/group automatically):
sudo chmod 640 /etc/ssl/private/cf-origin.key
sudo chown root:nginx /etc/ssl/private/cf-origin.key

# If Nginx is NOT installed yet, use root:root temporarily:
# sudo chmod 600 /etc/ssl/private/cf-origin.key
# sudo chown root:root /etc/ssl/private/cf-origin.key
# Then after installing Nginx (section 4.1), update permissions:
# sudo chmod 640 /etc/ssl/private/cf-origin.key
# sudo chown root:nginx /etc/ssl/private/cf-origin.key
```

### 3.6 Configure Protection (Optional but Recommended)

**Web Application Firewall (WAF):**
- Go to **Security** → **Overview** (or **Security** → **Web app exploits**)
- On Free plan, basic WAF protection is enabled by default
- You can view managed rules under **Security** → **WAF** (if available) or **Security** → **Web app exploits**
- Ensure detection tools are running (should show "2/2 running")

**Bot Traffic Management (Free plan alternative):**
- Go to **Security** → **Bots** (or **Security** → **Bot Fight Mode**)
- Enable **Bot Fight Mode** (available on Free plan)
- This helps protect against automated bot attacks
- **Note:** Advanced rate limiting requires a paid plan, but Bot Fight Mode provides basic protection on Free plan

---

## 4. Nginx Configuration

### 4.1 Install Nginx

```bash
# Install Nginx
dnf install -y nginx

# Enable and start
systemctl enable nginx
systemctl start nginx
```

### 4.2 Configure Real IP (Cloudflare)

Get Cloudflare IP ranges:
```bash
# Download Cloudflare IP ranges
curl -s https://www.cloudflare.com/ips-v4 -o /tmp/cloudflare-ips-v4.txt
curl -s https://www.cloudflare.com/ips-v6 -o /tmp/cloudflare-ips-v6.txt
```

Edit Nginx config:
```bash
nano /etc/nginx/nginx.conf
```

Add inside `http` block (before any `server` blocks):
```nginx
# Cloudflare real IP configuration
set_real_ip_from 103.21.244.0/22;
set_real_ip_from 103.22.200.0/22;
set_real_ip_from 103.31.4.0/22;
set_real_ip_from 104.16.0.0/13;
set_real_ip_from 104.24.0.0/14;
set_real_ip_from 108.162.192.0/18;
set_real_ip_from 131.0.72.0/22;
set_real_ip_from 141.101.64.0/18;
set_real_ip_from 162.158.0.0/15;
set_real_ip_from 172.64.0.0/13;
set_real_ip_from 173.245.48.0/20;
set_real_ip_from 188.114.96.0/20;
set_real_ip_from 190.93.240.0/20;
set_real_ip_from 197.234.240.0/22;
set_real_ip_from 198.41.128.0/17;
set_real_ip_from 2400:cb00::/32;
set_real_ip_from 2606:4700::/32;
set_real_ip_from 2803:f800::/32;
set_real_ip_from 2405:b500::/32;
set_real_ip_from 2405:8100::/32;
set_real_ip_from 2c0f:f248::/32;
set_real_ip_from 2a06:98c0::/29;

real_ip_header CF-Connecting-IP;
```

### 4.3 Landing Page Virtual Host

**Note:** On Fedora, Nginx configs go in `/etc/nginx/conf.d/` (not `sites-available`).

```bash
sudo nano /etc/nginx/conf.d/landing.conf
```

```nginx
server {
    listen 80;
    listen [::]:80;
    server_name <domain>.com www.<domain>.com;
    
    # Redirect HTTP to HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl;
    listen [::]:443 ssl;
    http2 on;
    server_name <domain>.com www.<domain>.com;

    # SSL Certificate (Cloudflare Origin)
    ssl_certificate /etc/ssl/certs/cf-origin.pem;
    ssl_certificate_key /etc/ssl/private/cf-origin.key;
    
    # SSL Configuration
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;

    # Root directory for landing page
    root /var/www/landing;
    index index.html;

    # Security Headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Referrer-Policy "no-referrer-when-downgrade" always;
    add_header Content-Security-Policy "default-src 'self' http: https: data: blob: 'unsafe-inline'" always;

    # Caching for static assets
    location ~* \.(jpg|jpeg|png|gif|ico|css|js|svg|woff|woff2|ttf|eot)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # Main location
    location / {
        try_files $uri $uri/ =404;
    }
}
```

### 4.4 Application Virtual Host

```bash
sudo nano /etc/nginx/conf.d/app.conf
```

```nginx
server {
    listen 80;
    listen [::]:80;
    server_name app.<domain>.com;
    
    # Redirect HTTP to HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl;
    listen [::]:443 ssl;
    http2 on;
    server_name app.<domain>.com;

    # SSL Certificate (Cloudflare Origin)
    ssl_certificate /etc/ssl/certs/cf-origin.pem;
    ssl_certificate_key /etc/ssl/private/cf-origin.key;
    
    # SSL Configuration
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;

    # Security Headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Referrer-Policy "no-referrer-when-downgrade" always;

    # Reverse proxy to Spring Boot (localhost only)
    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        
        # Forward headers
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-Forwarded-Host $host;
        
        # WebSocket support (if needed)
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        
        # Timeouts
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }
}
```

### 4.5 Enable Sites

```bash
# Create landing page directory
mkdir -p /var/www/landing
chown deploy:deploy /var/www/landing

# Enable sites
# On Fedora, files in /etc/nginx/conf.d/ are automatically included
# No symlinks needed - the .conf files are already active

# Remove default site (if it exists on Fedora)
sudo rm -f /etc/nginx/conf.d/default.conf

# Test configuration
sudo nginx -t

# Reload Nginx
sudo systemctl reload nginx
```

---

## 5. Azure Entra ID Setup

### 5.1 Create App Registration

1. Go to **Azure Portal** → **Azure Active Directory** → **App registrations**
2. Click **New registration**
3. Fill in:
   - **Name**: `Clear Chain`
   - **Supported account types**: Your organization only (or appropriate)
   - **Redirect URI**: 
     - Type: **Web**
     - URI: `https://app.<domain>.com/login/oauth2/code/azure`

4. Click **Register**

### 5.2 Capture Credentials

After registration:
- **Application (client) ID** → `AZURE_CLIENT_ID`
- **Directory (tenant) ID** → `AZURE_TENANT_ID`

### 5.3 Create Client Secret

1. Go to **Certificates & secrets**
2. Click **New client secret**
3. Description: `Production secret`
4. Expires: Choose appropriate (recommend 24 months for production)
5. Click **Add**
6. **Copy the secret value immediately** → `AZURE_CLIENT_SECRET`
   - ⚠️ **Warning**: Secret value is shown only once!

### 5.4 Configure Group/Role Claims

Choose **Option A** (Groups) or **Option B** (App Roles):

#### Option A: Entra Groups (Recommended)

1. Go to **Token configuration**
2. Click **Add groups claim**
3. Select:
   - **Security groups**
   - **Group ID** (in ID token and Access token)
4. Save

#### Option B: App Roles

1. Go to **App roles**
2. Click **Create app role**
3. Create roles matching your Spring Security roles:
   - **Display name**: `Admin`
   - **Value**: `ROLE_ADMIN`
   - **Allowed member types**: Users/Groups
   - **Description**: Administrator access

   Repeat for other roles:
   - `ROLE_COMPLIANCE_OFFICER`
   - `ROLE_FINANCE_OFFICER`
   - `ROLE_DEPARTMENT_REQUESTER`
   - etc.

4. **Assign users (Note: Group assignments require Azure AD Premium):**
   - Go to **Enterprise applications** → Your app → **Users and groups**
   - Click **Add user/group**
   - **Note:** If you see a warning about groups not being available, you're on a free/basic plan. You can only assign individual users, not groups. This is fine for most use cases.
   - Select individual users and assign app role
   - For group-based access, you would need Azure AD Premium (P1 or P2)

### 5.5 Configure API Permissions (Optional)

For group overage handling (Option A only):
- **Microsoft Graph** → **Delegated permissions**
- Add: `GroupMember.Read.All` (for reading group membership)

---

## 6. Application Deployment

### 6.1 Deploy Application Code

Clone repository on server:
```bash
cd /opt/myapp
su - deploy
git clone https://github.com/da-luiz/Clear-Chain.git .
```

Or upload files via SCP:
```bash
# From your local machine
scp -r . deploy@<DROPLET_IP>:/opt/myapp/
```

### 6.2 Create Environment File

```bash
cd /opt/myapp
nano .env
```

```bash
# Profile
SPRING_PROFILES_ACTIVE=prod

# Azure Entra ID
AZURE_TENANT_ID=your-tenant-id
AZURE_CLIENT_ID=your-client-id
AZURE_CLIENT_SECRET=your-client-secret

# Database (PostgreSQL recommended)
# Note: Use host.docker.internal instead of localhost when app runs in Docker
SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/vendor_db
SPRING_DATASOURCE_USERNAME=vendor_user
SPRING_DATASOURCE_PASSWORD=secure-password-here
SPRING_DATASOURCE_DRIVER=org.postgresql.Driver
SPRING_JPA_DATABASE_PLATFORM=org.hibernate.dialect.PostgreSQLDialect

# JWT
JWT_SECRET=your-very-long-random-secret-key-min-256-bits-change-this
JWT_EXPIRATION=86400

# Server
SERVER_PORT=8080

# Production logging
SPRING_JPA_SHOW_SQL=false
SPRING_JPA_FORMAT_SQL=false
LOGGING_LEVEL_ROOT=INFO

# OAuth2 (optional - for Google/GitHub login)
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
GITHUB_CLIENT_ID=your-github-client-id
GITHUB_CLIENT_SECRET=your-github-client-secret
```

**Secure the file:**
```bash
chmod 600 .env
chown deploy:deploy .env
```

### 6.3 Start Application

```bash
cd /opt/myapp
docker compose up -d --build

# Check logs
docker compose logs -f app

# Check status
docker compose ps
```

---

## 7. Landing Page

### 7.1 Create Landing Page HTML

Create `/var/www/landing/index.html` (see `deployment/landing/index.html` in repository).

### 7.2 Set Permissions

```bash
chown -R deploy:deploy /var/www/landing
chmod -R 755 /var/www/landing
```

---

## 8. Runbook

See `README-deploy.md` (created separately) for:
- Start/stop/update/rollback commands
- Configuration locations
- Common failure modes and fixes

---

## Verification Checklist

- [ ] Droplet accessible via SSH (key-based only)
- [ ] Firewall allows only 22/80/443
- [ ] Docker and Docker Compose installed
- [ ] Cloudflare DNS configured (A records, proxied)
- [ ] Cloudflare SSL/TLS: Full (strict)
- [ ] Origin certificate installed on Droplet
- [ ] Nginx serving landing page (HTTPS)
- [ ] Nginx reverse proxying to app (HTTPS)
- [ ] App container running (port 8080 on localhost only)
- [ ] Azure Entra ID login works
- [ ] Group/role mapping works
- [ ] Droplet reboot restores services

---

## Troubleshooting

### Application Not Starting

```bash
# Check container logs
docker compose logs app

# Check container status
docker compose ps

# Restart container
docker compose restart app
```

### Nginx Errors

```bash
# Test configuration
nginx -t

# Check Nginx logs
tail -f /var/log/nginx/error.log

# Check access logs
tail -f /var/log/nginx/access.log
```

### SSL/TLS Issues

- Verify origin certificate is correct
- Check Cloudflare SSL/TLS mode is "Full (strict)"
- Verify certificate files have correct permissions

### Azure Login Issues

- Verify redirect URI matches exactly
- Check client ID, secret, tenant ID
- Verify Azure app registration is active
- Check token claims in browser developer tools

---

## DigitalOcean-Specific Tips

### Monitoring

DigitalOcean provides built-in monitoring:
1. Go to **Droplet** → **Monitoring** tab
2. Enable **Monitoring** (free)
3. Set up alerts for CPU, memory, disk usage

### Backups

Enable automatic backups:
1. Go to **Droplet** → **Backups** tab
2. Enable **Backups** (additional cost)
3. Backups run daily and are retained for 7 days

### Firewall (Alternative to UFW)

DigitalOcean also provides a cloud firewall:
1. Go to **Networking** → **Firewalls**
2. Create firewall rules
3. Apply to your droplet

### Snapshots

Create manual snapshots before major changes:
1. Go to **Droplet** → **Snapshots**
2. Click **Take Snapshot**
3. Can restore entire droplet from snapshot

---

## Next Steps

1. Complete Azure Entra ID group/role mapping implementation
2. Test end-to-end authentication flow
3. Deploy landing page
4. Set up monitoring and logging
5. Configure backups
6. Set up automated deployments (CI/CD)

---

## Cost Estimation

**DigitalOcean Droplet:**
- Basic 1 vCPU, 2GB RAM, 50GB SSD, 2TB transfer: ~$12/month

**Cloudflare:**
- Free plan: $0/month (sufficient for most use cases)

**Azure Entra ID:**
- Free tier: $0/month (up to 50,000 objects)

**Total estimated cost:** ~$12/month (excluding domain registration)

**Note:** The 2GB RAM configuration is suitable for small to medium applications. If you experience performance issues, consider upgrading to 4GB RAM (~$24/month).
