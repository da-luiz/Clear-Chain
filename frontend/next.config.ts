import type { NextConfig } from 'next'

const nextConfig: NextConfig = {
  // Allow self-signed certificates in development
  // This is needed for HTTPS localhost with self-signed certificates
  async rewrites() {
    return []
  },
  // Output configuration for standalone deployment
  output: 'standalone',
}

export default nextConfig





