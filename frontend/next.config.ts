import type { NextConfig } from 'next'

const nextConfig: NextConfig = {
  // Allow self-signed certificates in development
  // This is needed for HTTPS localhost with self-signed certificates
  async rewrites() {
    return []
  },
  // Disable Turbopack to use traditional webpack mode
  // This prevents the "Next.js package not found" panic errors
  experimental: {
    turbo: false,
  },
}

export default nextConfig





