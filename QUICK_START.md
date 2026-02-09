# Quick Start Checklist - DigitalOcean Deployment

Follow these steps **in order** to avoid SSH connection issues:

## Step 1: Setup SSH Keys (5 minutes)

```bash
# Generate SSH key (if you don't have one)
ssh-keygen -t ed25519 -C "your_email@example.com" -f ~/.ssh/digitalocean_key

# Set permissions
chmod 600 ~/.ssh/digitalocean_key
chmod 644 ~/.ssh/digitalocean_key.pub

# Display your public key
cat ~/.ssh/digitalocean_key.pub
```

**Copy the entire output** and add it to DigitalOcean:
1. DigitalOcean Dashboard → Settings → Security → SSH Keys
2. Click "Add SSH Key"
3. Paste your public key
4. Name it (e.g., "Fedora Laptop")
5. Click "Add SSH Key"

## Step 2: Create Droplet

1. DigitalOcean Dashboard → Create → Droplets
2. Choose:
   - **Image**: Fedora (latest)
   - **Plan**: Basic, 1 vCPU, 2GB RAM, 50GB SSD
   - **Region**: Nearest to your users
   - **Authentication**: **SELECT YOUR SSH KEY** (the one you just added)
   - **Hostname**: `clear-chain-vps`
3. Click "Create Droplet"
4. **Note the IP address**

## Step 3: Create SSH Config (Optional but Recommended)

```bash
# Replace YOUR_DROPLET_IP with the actual IP
cat >> ~/.ssh/config << EOF
Host clear-chain-vps
    HostName YOUR_DROPLET_IP
    User root
    IdentityFile ~/.ssh/digitalocean_key
    IdentitiesOnly yes
    StrictHostKeyChecking no
EOF

chmod 600 ~/.ssh/config
```

## Step 4: Test Connection

```bash
ssh clear-chain-vps
# Or: ssh -i ~/.ssh/digitalocean_key root@YOUR_DROPLET_IP
```

**If it works, you're ready!** Continue with the main deployment guide.

**If it fails:**
- Make sure you selected the SSH key when creating the droplet
- Verify permissions: `chmod 600 ~/.ssh/digitalocean_key`
- Check that your public key is in DigitalOcean's SSH keys list

## Step 5: Follow Main Deployment Guide

Continue with `DIGITALOCEAN_DEPLOYMENT.md` starting from section 1.2
