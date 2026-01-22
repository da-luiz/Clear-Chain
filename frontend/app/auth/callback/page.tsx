'use client'

import { useEffect, Suspense } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'

function OAuthCallbackContent() {
  const router = useRouter()
  const searchParams = useSearchParams()

  useEffect(() => {
    const token = searchParams.get('token')
    const username = searchParams.get('username')
    const role = searchParams.get('role')
    const userId = searchParams.get('userId')
    const firstName = searchParams.get('firstName')
    const lastName = searchParams.get('lastName')
    const error = searchParams.get('error')

    if (error) {
      // Redirect to login with error
      router.push(`/login?error=${encodeURIComponent(error)}`)
      return
    }

    if (token && username && role && userId) {
      // Store token and user info in localStorage
      localStorage.setItem('token', token)
      localStorage.setItem('user', JSON.stringify({
        username,
        role,
        userId: parseInt(userId),
        firstName: firstName || '',
        lastName: lastName || '',
      }))

      // Redirect to dashboard
      router.push('/')
    } else {
      // Missing required parameters
      router.push('/login?error=oauth_callback_invalid')
    }
  }, [searchParams, router])

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="text-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
        <p className="mt-4 text-gray-600">Completing sign in...</p>
      </div>
    </div>
  )
}

export default function OAuthCallbackPage() {
  return (
    <Suspense fallback={
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
          <p className="mt-4 text-gray-600">Loading...</p>
        </div>
      </div>
    }>
      <OAuthCallbackContent />
    </Suspense>
  )
}







