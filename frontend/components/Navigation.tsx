'use client'

import Link from 'next/link'
import { usePathname, useRouter } from 'next/navigation'
import { useEffect, useState } from 'react'
import { getCurrentUser, canViewDashboard, canCreateVendorRequest, canViewVendors, canCreatePurchaseOrder, canCreateContract, canManageUsers, getRoleDisplayName, User } from '@/lib/permissions'

export default function Navigation() {
  const pathname = usePathname()
  const router = useRouter()
  const [user, setUser] = useState<User | null>(null)
  const [navItems, setNavItems] = useState<Array<{ href: string; label: string; show: boolean }>>([])

  useEffect(() => {
    const currentUser = getCurrentUser()
    setUser(currentUser)

    // Build navigation items based on user permissions
    const items = [
      { 
        href: '/', 
        label: 'Dashboard', 
        show: canViewDashboard(currentUser) 
      },
      { 
        href: '/vendor-requests', 
        label: 'Vendor Requests', 
        show: canCreateVendorRequest(currentUser) || canViewDashboard(currentUser) // Anyone who can view dashboard can see vendor requests
      },
      { 
        href: '/vendors', 
        label: 'Vendors', 
        show: canViewVendors(currentUser) 
      },
      { 
        href: '/purchase-orders', 
        label: 'Purchase Orders', 
        show: canCreatePurchaseOrder(currentUser) || canViewDashboard(currentUser) // Anyone authenticated can view
      },
      { 
        href: '/contracts', 
        label: 'Contracts', 
        show: canCreateContract(currentUser) || canViewDashboard(currentUser) // Anyone authenticated can view
      },
      { 
        href: '/users', 
        label: 'Users', 
        show: canManageUsers(currentUser) 
      },
    ]

    setNavItems(items)

    // Redirect to login if not authenticated (except on login and register pages)
    if (!currentUser && pathname !== '/login' && pathname !== '/register') {
      router.push('/login')
    }
  }, [pathname, router])

  const handleLogout = () => {
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    router.push('/login')
  }

  // Don't show navigation on login or register pages
  if (pathname === '/login' || pathname === '/register') {
    return null
  }

  return (
    <nav className="bg-blue-600 text-white shadow-lg">
      <div className="container mx-auto px-4">
        <div className="flex items-center justify-between h-16">
          <div className="flex items-center">
            <Link href="/" className="text-xl font-bold">
              Clear Chain
            </Link>
          </div>
          <div className="flex items-center space-x-4">
            <div className="flex space-x-4">
              {navItems.filter(item => item.show).map((item) => (
                <Link
                  key={item.href}
                  href={item.href}
                  className={`px-3 py-2 rounded-md text-sm font-medium transition-colors ${
                    pathname === item.href
                      ? 'bg-blue-700 text-white'
                      : 'text-blue-100 hover:bg-blue-700 hover:text-white'
                  }`}
                >
                  {item.label}
                </Link>
              ))}
            </div>
            {user && (
              <div className="flex items-center space-x-4 ml-4 pl-4 border-l border-blue-500">
                <div className="text-sm">
                  <div className="font-medium">{user.firstName} {user.lastName}</div>
                  <div className="text-blue-200 text-xs">{getRoleDisplayName(user.role)}</div>
                </div>
                <button
                  onClick={handleLogout}
                  className="px-3 py-2 rounded-md text-sm font-medium text-blue-100 hover:bg-blue-700 hover:text-white transition-colors"
                >
                  Logout
                </button>
              </div>
            )}
          </div>
        </div>
      </div>
    </nav>
  )
}

