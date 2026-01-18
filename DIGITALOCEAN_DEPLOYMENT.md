# DigitalOcean Deployment Guide

This guide will help you deploy your Clear Chain application (Spring Boot backend + Next.js frontend) to DigitalOcean App Platform.

## Deployment Architecture

You'll deploy **two separate apps**:
1. **Backend App** - Spring Boot API (runs on port 8080)
2. **Frontend App** - Next.js application (runs on port 3000)

## Prerequisites

1. **DigitalOcean Account** - Sign up at [digitalocean.com](https://www.digitalocean.com)
2. **GitHub Repository** - Your code must be in GitHub (already done)
3. **PostgreSQL Database** (recommended for production) - Can be created in DigitalOcean

## Step 1: Prepare Environment Variables

### Backend Environment Variables

You'll need to set these in DigitalOcean App Platform:

```bash
# Database (PostgreSQL recommended for production)
SPRING_DATASOURCE_URL=jdbc:postgresql://<db-host>:5432/<db-name>
SPRING_DATASOURCE_USERNAME=<db-username>
SPRING_DATASOURCE_PASSWORD=<db-password>
SPRING_DATASOURCE_DRIVER=org.postgresql.Driver
SPRING_JPA_DATABASE_PLATFORM=org.hibernate.dialect.PostgreSQLDialect

# JWT Configuration
JWT_SECRET=your-very-long-random-secret-key-min-256-bits-change-this-in-production
JWT_EXPIRATION=86400

# OAuth (Optional - if using OAuth)
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret
GITHUB_CLIENT_ID=your-github-client-id
GITHUB_CLIENT_SECRET=your-github-client-secret

# Server Port (DigitalOcean will set this automatically, but include for reference)
SERVER_PORT=8080
```

### Frontend Environment Variables

```bash
# Backend API URL (will be set after backend is deployed)
NEXT_PUBLIC_API_URL=https://your-backend-app.ondigitalocean.app/api
```

## Step 2: Update Frontend API Configuration

The frontend needs to use the production API URL. Update `frontend/lib/api.ts` to use environment variable:

```typescript
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';
```

## Step 3: Deploy Backend (Spring Boot)

### 3.1 Create Backend App in DigitalOcean

1. Go to **DigitalOcean Dashboard** → **Apps** → **Create App**
2. Choose **GitHub** as source
3. Select your repository: `da-luiz/Clear-Chain`
4. Select branch: `main`

### 3.2 Configure Backend App

**App Spec:**
- **Name**: `clear-chain-backend` (or your preferred name)
- **Type**: **Web Service**
- **Source Directory**: `/` (root)
- **Build Command**: `./gradlew build -x test`
- **Run Command**: `java -jar build/libs/Clear\ Chain-0.0.1-SNAPSHOT.jar`
- **HTTP Port**: `8080`
- **Environment**: `production`

**Environment Variables:**
- Add all backend environment variables from Step 1

**Resources:**
- **Plan**: Basic ($5/month minimum) or Professional
- **Instance Size**: Basic - Regular (1 vCPU, 1GB RAM) is minimum for Spring Boot

### 3.3 Add PostgreSQL Database (Recommended)

1. In App Platform, go to **Components** tab
2. Click **+ Add Component** → **Database**
3. Choose **PostgreSQL**
4. Select version (14+ recommended)
5. Choose plan ($15/month minimum for managed DB)

**Update Database URL:**
After creating the database, you'll get a connection string. Update `SPRING_DATASOURCE_URL` environment variable.

### 3.4 Deploy Backend

Click **Create Resources** and wait for deployment (5-10 minutes).

**Note Backend URL:**
After deployment, your backend will be available at:
`https://clear-chain-backend-xxxxx.ondigitalocean.app`

Use this URL in the frontend environment variable: `NEXT_PUBLIC_API_URL`

## Step 4: Deploy Frontend (Next.js)

### 4.1 Create Frontend App

1. Go to **DigitalOcean Dashboard** → **Apps** → **Create App**
2. Choose **GitHub** as source
3. Select your repository: `da-luiz/Clear-Chain`
4. Select branch: `main`

### 4.2 Configure Frontend App

**App Spec:**
- **Name**: `clear-chain-frontend` (or your preferred name)
- **Type**: **Web Service**
- **Source Directory**: `frontend`
- **Build Command**: `npm install && npm run build`
- **Run Command**: `npm start`
- **HTTP Port**: `3000`
- **Environment**: `production`

**Environment Variables:**
- `NEXT_PUBLIC_API_URL`: `https://clear-chain-backend-xxxxx.ondigitalocean.app/api`

**Resources:**
- **Plan**: Basic ($5/month minimum)
- **Instance Size**: Basic - Regular (1 vCPU, 1GB RAM)

### 4.3 Deploy Frontend

Click **Create Resources** and wait for deployment (3-5 minutes).

## Step 5: Update CORS Configuration

Ensure your backend CORS config allows your frontend domain:

```java
@CrossOrigin(origins = {
    "http://localhost:3000",
    "https://clear-chain-frontend-xxxxx.ondigitalocean.app"
})
```

Or use environment variable for production origin.

## Step 6: Custom Domains (Optional)

1. Go to your app settings in DigitalOcean
2. Click **Settings** → **Domains**
3. Add your custom domain
4. Follow DNS configuration instructions

## Troubleshooting

### Backend Issues

**Build Fails:**
- Check Java version (need Java 17+)
- Verify Gradle build command
- Check build logs in DigitalOcean

**Database Connection Fails:**
- Verify database connection string
- Check database is running and accessible
- Verify firewall rules allow connection

**Port Issues:**
- DigitalOcean sets `PORT` environment variable automatically
- Spring Boot should read from `PORT` or `SERVER_PORT`
- Update `application.properties` to use: `server.port=${PORT:8080}`

### Frontend Issues

**API Connection Fails:**
- Verify `NEXT_PUBLIC_API_URL` is set correctly
- Check backend is running and accessible
- Check CORS configuration in backend
- Check browser console for CORS errors

**Build Fails:**
- Check Node.js version (need 18+)
- Verify `package.json` is in `frontend/` directory
- Check build logs for specific errors

## Cost Estimate

**Minimum Setup:**
- Backend App: $5/month
- Frontend App: $5/month
- PostgreSQL Database: $15/month
- **Total: ~$25/month**

**With Professional Plan:**
- Higher costs but better performance and auto-scaling

## Alternative: Deploy to Single Droplet

If you want more control and lower cost:

1. Create a DigitalOcean Droplet (Ubuntu 22.04)
2. Install Java 17, Node.js 18, PostgreSQL
3. Set up Nginx as reverse proxy
4. Run both apps on the same server
5. Use systemd services to manage processes

Cost: ~$6/month for basic droplet

## Next Steps

1. Test the deployed application
2. Set up monitoring and logging
3. Configure backups for database
4. Set up CI/CD for automatic deployments
5. Add SSL certificates (automatic with App Platform)

