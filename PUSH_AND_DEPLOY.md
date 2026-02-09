# Push and Deploy – How to Get Updates Live

After you change the app (landing page, login, logo, backend, etc.), follow these steps so the live site reflects your updates.

---

## 1. Push your code to Git

From your **local machine** in the project root:

```bash
cd /home/ovi/IdeaProjects/Clear-Chain

# Stage and commit (if not already done)
git add -A
git status
git commit -m "Landing and login updates, add logo, two-panel login"

# Push to your remote (e.g. origin main)
git push origin main
```

Use your real branch name if it’s not `main` (e.g. `master` or another branch your server pulls from).

---

## 2. On the server: pull and run the deploy script

SSH into your droplet and run the full deploy script. This will:

- Pull the latest code
- Build the backend (Gradle)
- Rebuild and start Docker (backend + frontend), so the **app** (including login and logo) updates
- Reload Nginx config
- Copy **landing** files (including `logo.svg` and `images/`) to `/var/www/landing` so **clearchain.space** updates

```bash
ssh clear-chain-vps
# or: ssh ovi@YOUR_DROPLET_IP

cd /opt/myapp
./deploy-full-app.sh
```

If you don’t have `deploy-full-app.sh` on the server yet, copy it from the repo (e.g. after `git pull`) or run the same steps manually (see below).

---

## 3. If you prefer to run steps manually (no script)

On the server:

```bash
cd /opt/myapp

# Pull latest code
git pull origin main

# Build backend
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk
./gradlew clean build -x test

# Rebuild and start app (backend + frontend)
docker compose down
docker compose up -d --build

# Reload Nginx
sudo cp nginx-app.conf /etc/nginx/conf.d/app.conf
sudo nginx -t && sudo systemctl reload nginx

# Update landing page (logo, index.html, images)
sudo cp -r deployment/landing/* /var/www/landing/
sudo chmod -R 755 /var/www/landing
```

---

## 4. Check that updates are live

- **Landing:** https://clearchain.space  
  - Logo in header, favicon, hero image, green theme, correct links.
- **App / Login:** https://app.clearchain.space  
  - Two-panel login, logo on the left, green button and teal “Forgot password?” link.
- **Favicon:** Both sites should show the CC favicon in the browser tab.

If something didn’t update (e.g. landing), confirm:

- `/var/www/landing` exists: `ls -la /var/www/landing`
- Nginx landing config uses `root /var/www/landing;` (e.g. in `/etc/nginx/conf.d/landing.conf`).

---

## Summary

| Step | Where   | Action |
|------|--------|--------|
| 1    | Local  | `git add` → `git commit` → `git push origin main` |
| 2    | Server | `cd /opt/myapp` → `./deploy-full-app.sh` |
| 3    | Browser | Open https://clearchain.space and https://app.clearchain.space to verify |

Logos are in place: **landing** uses `deployment/landing/logo.svg` (and favicon), **app** uses `frontend/public/logo.svg` (login + app favicon). The deploy script copies the whole `deployment/landing/` folder (including `logo.svg` and `images/`) to `/var/www/landing`, so pushing and running the script is enough for the app and landing updates to go live.
