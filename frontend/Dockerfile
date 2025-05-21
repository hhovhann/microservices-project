# 1. Base image
FROM node:20-alpine AS builder

# 2. Set working directory
WORKDIR /app

# 3. Copy and install dependencies
COPY package*.json ./
RUN npm install

# 4. Copy source and build
COPY . .
RUN npm run build

# 5. Production image
FROM node:20-alpine

WORKDIR /app

COPY --from=builder /app/package*.json ./
RUN npm install --omit=dev

COPY --from=builder /app/.next .next
COPY --from=builder /app/public ./public
COPY --from=builder /app/next.config.ts ./

EXPOSE 3000
CMD ["npm", "start"]
