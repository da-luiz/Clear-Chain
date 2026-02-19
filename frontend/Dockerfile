# Build stage
FROM node:20-alpine AS builder
WORKDIR /app

COPY package*.json ./
COPY tsconfig.json ./
COPY next.config.ts ./
COPY next.config.js ./
COPY tailwind.config.ts ./
COPY postcss.config.js ./
RUN npm install

COPY . .
RUN mkdir -p public && touch public/.gitkeep
RUN npm run build

# Run stage
FROM node:20-alpine AS runner
WORKDIR /app
ENV NODE_ENV=production
COPY --from=builder /app/.next ./.next
COPY --from=builder /app/public ./public
COPY --from=builder /app/package*.json ./
COPY --from=builder /app/next.config.ts ./
COPY --from=builder /app/next.config.js ./
RUN npm install --only=production
EXPOSE 3000
CMD ["npm", "start"]
