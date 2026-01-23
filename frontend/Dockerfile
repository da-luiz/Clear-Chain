# Build stage
FROM node:20-alpine AS builder

WORKDIR /app

# Copy package files
COPY package*.json ./
COPY tsconfig.json ./
COPY next.config.ts ./
COPY next.config.js ./
COPY tailwind.config.ts ./
COPY postcss.config.js ./

# Install dependencies (use npm install to handle lock file mismatches)
RUN npm install

# Copy source code
COPY . .

# Ensure public directory exists (Next.js may need it)
RUN mkdir -p public && touch public/.gitkeep

# Build Next.js app
RUN npm run build

# Production stage
FROM node:20-alpine AS runner

WORKDIR /app

ENV NODE_ENV=production

# Copy built application
COPY --from=builder /app/.next ./.next
# Copy public directory (will be empty if no files, but directory exists)
COPY --from=builder /app/public ./public
COPY --from=builder /app/package*.json ./
COPY --from=builder /app/next.config.ts ./
COPY --from=builder /app/next.config.js ./

# Install only production dependencies
RUN npm install --only=production

EXPOSE 3000

ENV PORT=3000
ENV HOSTNAME="0.0.0.0"

CMD ["npm", "start"]
