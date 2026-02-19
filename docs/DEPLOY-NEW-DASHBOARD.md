# Deploy and dashboard

## "no configuration file provided: not found"

If you see this when running `docker compose`, the repo now includes `docker-compose.yml` in the project root. Do this:

1. **Push from your dev machine** (so GitHub has the latest, including `docker-compose.yml` and the Dockerfiles):
   ```bash
   cd ~/IdeaProjects/Clear-Chain
   git add docker-compose.yml Dockerfile frontend/Dockerfile docs/
   git commit -m "Add docker-compose and Dockerfiles for server deploy"
   git push origin master
   ```

2. **On the server**, pull and run:
   ```bash
   cd /opt/myapp
   git pull origin master
   docker compose build --no-cache frontend
   docker compose up -d
   ```

If the **backend** build fails (e.g. missing `build.gradle` or `gradlew`), those files must exist in the repo or be restored on the server (e.g. from a backup or from the same place you had the deploy script).

---

## Why the dashboard still shows the old design after deploy

Docker reuses **cached** image layers. If the deploy log shows `CACHED` for all frontend build steps, the new UI was never built into the image.

**Fix:** rebuild frontend without cache, then bring the stack up:

```bash
cd /opt/myapp
docker compose build --no-cache frontend
docker compose up -d
```

Then hard-refresh **https://app.clearchain.space** (Ctrl+Shift+R).

---

## Optional: make future deploys always rebuild frontend

In `deploy-full-app.sh`, use:

```bash
docker compose build --no-cache frontend
docker compose up -d
```
