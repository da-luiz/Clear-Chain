# Deployment Runbook - Clear Chain

This runbook provides commands and procedures for managing the Clear Chain application deployment on DigitalOcean Droplet.

## Directory Structure

```
/opt/myapp/
├── docker-compose.yml    # Docker Compose configuration
├── .env                  # Environment variables (chmod 600)
├── README-deploy.md      # This file
└── uploads/              # File uploads directory
```

## Configuration Locations

### Application
- **Docker Compose**: `/opt/myapp/docker-compose.yml`
- **Environment Variables**: `/opt/myapp/.env` (chmod 600)
- **Application Logs**: `docker compose logs app`

### Nginx
- **Main Config**: `/etc/nginx/nginx.conf`
- **Landing Page Config**: `/etc/nginx/sites-available/landing` → `/etc/nginx/sites-enabled/landing`
- **App Config**: `/etc/nginx/sites-available/app` → `/etc/nginx/sites-enabled/app`
- **Nginx Logs**: 
  - `/var/log/nginx/access.log`
  - `/var/log/nginx/error.log`

### SSL Certificates
- **Origin Certificate**: `/etc/ssl/certs/cf-origin.pem`
- **Private Key**: `/etc/ssl/private/cf-origin.key` (chmod 600)

### Landing Page
- **Directory**: `/var/www/landing/`
- **Index**: `/var/www/landing/index.html`

## Quick Commands

### Start Application
```bash
cd /opt/myapp
docker compose up -d
```

### Stop Application
```bash
cd /opt/myapp
docker compose down
```

### Restart Application
```bash
cd /opt/myapp
docker compose restart app
```

### View Logs
```bash
cd /opt/myapp
docker compose logs -f app
```

### View Container Status
```bash
cd /opt/myapp
docker compose ps
```

### Rebuild and Restart
```bash
cd /opt/myapp
docker compose down
docker compose up -d --build
```

## Update/Rollback Procedures

### Update Application

1. **Backup current deployment:**
```bash
cd /opt/myapp
cp .env .env.backup
docker compose ps > container-state.txt
```

2. **Pull latest code:**
```bash
cd /opt/myapp
git pull origin main  # Or copy new files
```

3. **Rebuild and restart:**
```bash
docker compose down
docker compose up -d --build
```

4. **Verify deployment:**
```bash
# Check container is running
docker compose ps

# Check logs for errors
docker compose logs app | tail -50

# Test application
curl -k https://app.<domain>.com/api/dashboard
```

### Rollback

1. **Restore previous version:**
```bash
cd /opt/myapp
git checkout <previous-commit-hash>
# Or restore files from backup
```

2. **Restart with previous version:**
```bash
docker compose down
docker compose up -d --build
```

3. **Restore environment file if needed:**
```bash
cp .env.backup .env
docker compose restart app
```

## Azure Entra ID Group/Role Mapping

### Configuration

Mapping is configured via environment variables in `/opt/myapp/.env`:

#### Option A: Entra Groups → Spring Roles

```bash
# Format: group-id-1:ROLE_ADMIN,group-id-2:ROLE_FINANCE_APPROVER
AZURE_GROUP_ROLE_MAPPING=<group-id-1>:ROLE_ADMIN,<group-id-2>:ROLE_FINANCE_APPROVER,<group-id-3>:ROLE_COMPLIANCE_OFFICER
```

**Example:**
```bash
AZURE_GROUP_ROLE_MAPPING=abc123-def456:ROLE_ADMIN,xyz789-uvw012:ROLE_FINANCE_APPROVER
```

#### Option B: Entra App Roles → Spring Roles

```bash
# Format: app-role-name:SPRING_ROLE,another-app-role:ANOTHER_SPRING_ROLE
AZURE_APP_ROLE_MAPPING=ROLE_ADMIN:ROLE_ADMIN,ROLE_FINANCE:ROLE_FINANCE_APPROVER
```

**Example:**
```bash
AZURE_APP_ROLE_MAPPING=Admin:ROLE_ADMIN,FinanceApprover:ROLE_FINANCE_APPROVER,ComplianceOfficer:ROLE_COMPLIANCE_OFFICER
```

### Available Spring Roles

- `ROLE_ADMIN` - Full system access
- `ROLE_DEPARTMENT_REQUESTER` - Can create vendor requests
- `ROLE_FINANCE_APPROVER` - Can approve at finance stage, add banking details
- `ROLE_COMPLIANCE_APPROVER` - Can approve at compliance stage

### How to Find Entra Group IDs

1. Go to **Azure Portal** → **Azure Active Directory** → **Groups**
2. Select the group
3. Copy the **Object ID** (this is the group ID)

### How to Find Entra App Roles

1. Go to **Azure Portal** → **App registrations** → Your app → **App roles**
2. Note the **Value** field (e.g., `ROLE_ADMIN`, `Admin`)

### Update Mapping

1. Edit environment file:
```bash
cd /opt/myapp
nano .env
# Update AZURE_GROUP_ROLE_MAPPING or AZURE_APP_ROLE_MAPPING
```

2. Restart application:
```bash
docker compose restart app
```

### Default Behavior

- **If `AZURE_ROLE_MAPPING_DENY_UNMAPPED=true`** (default): Users without mapped roles are denied access
- **If `AZURE_ROLE_MAPPING_DENY_UNMAPPED=false`**: Users without mapped roles get `ROLE_DEPARTMENT_REQUESTER` (default minimal role)

## Nginx Management

### Test Configuration
```bash
nginx -t
```

### Reload Configuration (no downtime)
```bash
systemctl reload nginx
```

### Restart Nginx
```bash
systemctl restart nginx
```

### Check Nginx Status
```bash
systemctl status nginx
```

### View Logs
```bash
# Access logs
tail -f /var/log/nginx/access.log

# Error logs
tail -f /var/log/nginx/error.log
```

## Common Failure Modes and Fixes

### Issue: Application Container Won't Start

**Symptoms:**
- `docker compose ps` shows container status as "Exited" or "Restarting"
- No application logs

**Diagnosis:**
```bash
cd /opt/myapp
docker compose logs app
```

**Common Causes & Fixes:**

1. **Database Connection Failed**
   - Check `SPRING_DATASOURCE_URL` in `.env`
   - Verify database is running (if PostgreSQL on same host)
   - Check network connectivity

2. **Azure Credentials Invalid**
   - Verify `AZURE_TENANT_ID`, `AZURE_CLIENT_ID`, `AZURE_CLIENT_SECRET`
   - Check Azure app registration is active
   - Verify redirect URI matches exactly

3. **Port Already in Use**
   - Check if port 8080 is bound: `netstat -tlnp | grep 8080`
   - Stop conflicting service or change port

### Issue: Nginx Returns 502 Bad Gateway

**Symptoms:**
- Application URL returns 502 error
- Nginx error log shows: `connect() failed (111: Connection refused)`

**Diagnosis:**
```bash
# Check if app container is running
docker compose ps

# Check if app is listening on 127.0.0.1:8080
netstat -tlnp | grep 8080

# Check Nginx error log
tail -20 /var/log/nginx/error.log
```

**Fixes:**
1. Start application container: `docker compose up -d`
2. Check app is binding to `127.0.0.1:8080` (not `0.0.0.0:8080`)
3. Verify docker-compose.yml port binding: `127.0.0.1:8080:8080`

### Issue: SSL Certificate Errors

**Symptoms:**
- Browser shows SSL/certificate error
- Cloudflare shows SSL error in dashboard

**Diagnosis:**
```bash
# Check certificate files exist
ls -la /etc/ssl/certs/cf-origin.pem
ls -la /etc/ssl/private/cf-origin.key

# Check certificate permissions
stat /etc/ssl/private/cf-origin.key  # Should be 600, root:root
```

**Fixes:**
1. Regenerate origin certificate in Cloudflare
2. Update certificate files on server
3. Restart Nginx: `systemctl restart nginx`
4. Verify Cloudflare SSL/TLS mode is "Full (strict)"

### Issue: Azure Login Fails

**Symptoms:**
- Redirect to Azure login works, but callback fails
- Error: "Invalid redirect URI"

**Diagnosis:**
```bash
# Check redirect URI in Azure app registration
# Should be: https://app.<domain>.com/login/oauth2/code/azure

# Check application logs
docker compose logs app | grep -i oauth
```

**Fixes:**
1. Verify redirect URI in Azure app registration matches exactly
2. Check `AZURE_CLIENT_ID`, `AZURE_CLIENT_SECRET`, `AZURE_TENANT_ID` in `.env`
3. Restart application: `docker compose restart app`

### Issue: Users Can't Access (No Role Mapping)

**Symptoms:**
- Azure login succeeds
- User gets "Access Denied" or default minimal role

**Diagnosis:**
```bash
# Check application logs for role mapping
docker compose logs app | grep -i "role\|mapping\|azure"

# Check environment variables
cd /opt/myapp
grep AZURE.*MAPPING .env
```

**Fixes:**
1. Verify group/role mapping is configured in `.env`
2. Check group IDs or app roles match Azure Entra ID
3. For group overage: May need to implement Microsoft Graph API call (advanced)

### Issue: Application Port Publicly Accessible

**Symptoms:**
- Can access `http://<VPS_IP>:8080` directly (should NOT be possible)

**Diagnosis:**
```bash
# Check firewall
ufw status

# Check if app is bound to 0.0.0.0 (wrong) or 127.0.0.1 (correct)
netstat -tlnp | grep 8080
# Should show: 127.0.0.1:8080 (not 0.0.0.0:8080)
```

**Fixes:**
1. Verify docker-compose.yml has: `"127.0.0.1:8080:8080"` (not `"8080:8080"`)
2. Verify firewall blocks port 8080: `ufw deny 8080`
3. Restart container: `docker compose restart app`

### Issue: VPS Reboot Services Don't Start

**Symptoms:**
- After reboot, application and Nginx don't start automatically

**Diagnosis:**
```bash
# Check Docker service
systemctl status docker

# Check if containers auto-start
docker compose ps

# Check Nginx service
systemctl status nginx
```

**Fixes:**
1. Enable Docker service: `systemctl enable docker`
2. Nginx should auto-start: `systemctl enable nginx` (usually already enabled)
3. Docker Compose auto-restarts containers with `restart: unless-stopped` (configured)
4. Create systemd service for docker-compose (optional):

```bash
sudo nano /etc/systemd/system/myapp.service
```

```ini
[Unit]
Description=Clear Chain Application
Requires=docker.service
After=docker.service

[Service]
Type=oneshot
RemainAfterExit=yes
WorkingDirectory=/opt/myapp
ExecStart=/usr/bin/docker compose up -d
ExecStop=/usr/bin/docker compose down
TimeoutStartSec=0

[Install]
WantedBy=multi-user.target
```

```bash
sudo systemctl enable myapp.service
sudo systemctl start myapp.service
```

## Maintenance Tasks

### Backup Database

```bash
# If using PostgreSQL on host (not in Docker)
pg_dump -U vendor_user vendor_db > /backup/vendor_db_$(date +%Y%m%d).sql

# If using PostgreSQL in Docker
docker exec <postgres-container> pg_dump -U vendor_user vendor_db > /backup/vendor_db_$(date +%Y%m%d).sql
```

### Update SSL Certificate

1. Go to Cloudflare → SSL/TLS → Origin Server
2. Generate new certificate
3. Copy certificate and key to server
4. Restart Nginx: `systemctl restart nginx`

### Clear Application Logs

```bash
# Docker logs
docker compose logs --tail=0 -f app

# Nginx logs (rotate)
logrotate -f /etc/logrotate.d/nginx
```

### Monitor Application Health

```bash
# Check container health
docker compose ps

# Check resource usage
docker stats

# Check disk space
df -h

# Check memory
free -h
```

## Emergency Procedures

### Complete Service Restart

```bash
# Stop everything
docker compose down
systemctl stop nginx

# Start everything
systemctl start nginx
docker compose up -d

# Verify
docker compose ps
systemctl status nginx
curl -k https://app.<domain>.com/api/dashboard
```

### Access Container Shell

```bash
docker compose exec app sh
# Or: docker exec -it clear-chain-app sh
```

### Check Environment Variables

```bash
docker compose exec app env | grep -E "AZURE|SPRING|JWT"
```

## Support Contacts

- **Infrastructure Issues**: Check DigitalOcean Dashboard
- **DNS/SSL Issues**: Check Cloudflare Dashboard
- **Azure Entra ID Issues**: Check Azure Portal → App registrations
- **Application Issues**: Check application logs: `docker compose logs app`

## Notes

- **Never commit `.env` file** - It contains sensitive credentials
- **Always backup `.env`** before changes
- **Test Nginx config** before reload: `nginx -t`
- **Check logs** first when troubleshooting
- **Container runs as non-root user** (spring:spring) for security



