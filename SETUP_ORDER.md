# Correct Setup Order - Don't Lock Yourself Out!

**CRITICAL: Follow these steps in EXACT order. Do NOT skip steps or change the order.**

## After Creating New Droplet:

### Step 1: Connect as Root
```bash
ssh clear-chain-vps
# Or: ssh -i ~/.ssh/clear-chain-key root@NEW_IP_ADDRESS
```

### Step 2: Create ovi User and Set Up SSH Keys
```bash
# Create user
useradd -m -s /bin/bash ovi

# Add to wheel group (for sudo)
usermod -aG wheel ovi

# Copy SSH keys to ovi user
mkdir -p /home/ovi/.ssh
cp ~/.ssh/authorized_keys /home/ovi/.ssh/authorized_keys
chown -R ovi:ovi /home/ovi/.ssh
chmod 700 /home/ovi/.ssh
chmod 600 /home/ovi/.ssh/authorized_keys
```

### Step 3: Set Password for ovi User (AS ROOT)
```bash
passwd ovi
# Enter password twice when prompted
```

### Step 4: Test Connection as ovi (From Your Local Machine)
```bash
# Exit server first
exit

# Update your local SSH config to use ovi
# Edit ~/.ssh/config and change User root to User ovi

# Test connection
ssh clear-chain-vps
```

### Step 5: Verify ovi User Works
Once logged in as ovi, test sudo:
```bash
sudo whoami
# Should ask for ovi's password and return "root"
```

### Step 6: ONLY AFTER ovi Works - Disable Root Login
```bash
# Edit SSH config
sudo nano /etc/ssh/sshd_config

# Find and change:
PermitRootLogin no
PasswordAuthentication no
PubkeyAuthentication yes

# Restart SSH
sudo systemctl restart sshd
```

### Step 7: Test Again
```bash
# Exit
exit

# Connect as ovi (should still work)
ssh clear-chain-vps
```

**If everything works, you're done with user setup!**
