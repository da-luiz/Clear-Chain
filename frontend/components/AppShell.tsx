'use client'

import Link from 'next/link'
import { usePathname, useRouter } from 'next/navigation'
import { useEffect, useState } from 'react'
import { getCurrentUser, canViewDashboard, canViewVendors, canCreatePurchaseOrder, canCreateContract, canManageUsers, canCreateVendorRequest, getRoleDisplayName, User } from '@/lib/permissions'

const navItems = [
  { href: '/', label: 'Dashboard', show: (u: User | null) => canViewDashboard(u), icon: 'dashboard' },
  { href: '/vendors', label: 'Vendors', show: (u: User | null) => canViewVendors(u), icon: 'briefcase' },
  { href: '/contracts', label: 'Contracts', show: (u: User | null) => canCreateContract(u) || canViewDashboard(u), icon: 'document' },
  { href: '/purchase-orders', label: 'Purchase Orders', show: (u: User | null) => canCreatePurchaseOrder(u) || canViewDashboard(u), icon: 'list' },
  { href: '/vendor-requests', label: 'Vendor Requests', show: (u: User | null) => canCreateVendorRequest(u) || canViewDashboard(u), icon: 'request' },
  { href: '/users', label: 'Users', show: (u: User | null) => canManageUsers(u), icon: 'users' },
]

function ChainIcon({ className }: { className?: string }) {
  return (
    <svg className={className} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71" />
      <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71" />
    </svg>
  )
}

function Icon({ name }: { name: string }) {
  switch (name) {
    case 'dashboard':
      return (
        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z" />
        </svg>
      )
    case 'briefcase':
      return (
        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 13.255A23.931 23.931 0 0112 15c-3.183 0-6.22-.62-9-1.745M16 6V4a2 2 0 00-2-2h-4a2 2 0 00-2 2v2m4 6h.01M5 20h14a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
        </svg>
      )
    case 'document':
      return (
        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
        </svg>
      )
    case 'list':
      return (
        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01" />
        </svg>
      )
    case 'request':
      return (
        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
        </svg>
      )
    case 'users':
      return (
        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
        </svg>
      )
    default:
      return null
  }
}

export default function AppShell({ children }: { children: React.ReactNode }) {
  const pathname = usePathname()
  const router = useRouter()
  const [user, setUser] = useState<User | null>(null)
  const [headerDate, setHeaderDate] = useState(() => new Date().toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' }))

  useEffect(() => {
    const currentUser = getCurrentUser()
    setUser(currentUser)
    if (!currentUser && pathname !== '/login' && pathname !== '/register') {
      router.push('/login')
    }
  }, [pathname, router])

  const handleRefresh = () => {
    setHeaderDate(new Date().toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' }))
    router.refresh()
  }

  if (pathname === '/login' || pathname === '/register') {
    return <>{children}</>
  }

  const visibleNavItems = navItems.filter((item) => item.show(user))

  return (
    <div className="flex min-h-screen bg-gray-50">
      {/* Left sidebar */}
      <aside className="w-56 bg-white border-r border-gray-200 flex flex-col shrink-0">
        <div className="p-4 border-b border-gray-100">
          <Link href="/" className="flex items-center gap-2 text-gray-800 font-semibold">
            <ChainIcon className="w-8 h-8 text-emerald-600" />
            <span>ClearChain</span>
          </Link>
        </div>
        <nav className="flex-1 p-3 space-y-0.5">
          {visibleNavItems.map((item) => {
            const isActive = pathname === item.href || (item.href !== '/' && pathname.startsWith(item.href))
            return (
              <Link
                key={item.href}
                href={item.href}
                className={`flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                  isActive
                    ? 'bg-emerald-50 text-emerald-700'
                    : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900'
                }`}
              >
                <Icon name={item.icon} />
                {item.label}
                {item.href === '/' && (
                  <svg className="w-4 h-4 ml-auto text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                )}
              </Link>
            )
          })}
        </nav>
        {user && (
          <div className="p-3 border-t border-gray-100">
            <div className="px-3 py-2 text-xs text-gray-500">
              <div className="font-medium text-gray-700">{user.firstName} {user.lastName}</div>
              <div>{getRoleDisplayName(user.role)}</div>
            </div>
            <button
              onClick={() => {
                localStorage.removeItem('token')
                localStorage.removeItem('user')
                router.push('/login')
              }}
              className="mt-2 w-full text-left px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 rounded-lg"
            >
              Logout
            </button>
          </div>
        )}
      </aside>

      {/* Main area: header + content */}
      <div className="flex-1 flex flex-col min-w-0">
        <header className="h-14 bg-white border-b border-gray-200 flex items-center justify-end gap-4 px-6 shrink-0">
          <span className="text-sm text-gray-600">{headerDate}</span>
          <button
            onClick={handleRefresh}
            className="flex items-center gap-2 px-3 py-2 text-sm font-medium text-gray-700 bg-gray-100 hover:bg-gray-200 rounded-lg transition-colors"
          >
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
            </svg>
            Refresh
          </button>
        </header>
        <main className="flex-1 p-6 overflow-auto">
          {children}
        </main>
      </div>
    </div>
  )
}
