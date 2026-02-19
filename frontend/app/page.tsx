'use client'

import { useEffect, useState } from 'react'
import Link from 'next/link'
import { getDashboard, DashboardSummary, ProcurementActivityPoint } from '@/lib/api'
import { getErrorMessage } from '@/lib/errorHandler'

export default function Dashboard() {
  const [dashboard, setDashboard] = useState<DashboardSummary | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [activityRange, setActivityRange] = useState<'7' | '30'>('30')

  useEffect(() => {
    loadDashboard()
  }, [])

  const loadDashboard = async () => {
    try {
      setLoading(true)
      setError(null)
      const data = await getDashboard()
      setDashboard(data)
    } catch (err: unknown) {
      setError(getErrorMessage(err) || 'Failed to load dashboard. Ensure the backend is running on http://localhost:8080')
      console.error('Error loading dashboard:', err)
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-[320px]">
        <div className="text-gray-500">Loading dashboard...</div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-xl">
        <strong className="font-semibold">Error: </strong>
        <span className="sm:inline">{error}</span>
      </div>
    )
  }

  if (!dashboard) {
    return <div className="text-gray-500">No data available</div>
  }

  const pendingVendorsCount = (dashboard.pendingVendorRequests ?? 0) + (dashboard.pendingVendors ?? 0)
  const approvedVendorsCount = dashboard.approvedVendors ?? dashboard.activeVendors ?? 0
  const activeContractsCount = dashboard.activeContractsCount ?? 0
  const purchaseOrdersCount = dashboard.totalPurchaseOrders ?? 0

  return (
    <div className="space-y-6">
      {/* Top row: 4 metric cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <MetricCard
          title="Pending Vendors"
          value={pendingVendorsCount}
          href="/vendor-requests"
        />
        <MetricCard
          title="Approved Vendors"
          value={approvedVendorsCount}
          href="/vendors"
        />
        <MetricCard
          title="Active Contracts"
          value={activeContractsCount}
          href="/contracts"
        />
        <MetricCard
          title="Purchase Orders"
          value={purchaseOrdersCount}
          href="/purchase-orders"
        />
      </div>

      {/* Middle row: Procurement Activity + Contracts expiring in 30 days */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <ProcurementActivityCard
          data={dashboard.procurementActivity ?? []}
          range={activityRange}
          onRangeChange={setActivityRange}
        />
        <ContractsExpiringCard contracts={dashboard.expiringContracts ?? []} />
      </div>

      {/* Bottom row: Recent Activity */}
      <RecentActivityCard items={dashboard.recentActivity ?? []} />
    </div>
  )
}

function MetricCard({
  title,
  value,
  href,
}: {
  title: string
  value: number
  href: string
}) {
  return (
    <Link
      href={href}
      className="bg-white rounded-xl border border-gray-100 p-6 shadow-sm hover:shadow-md hover:border-emerald-100 transition-all"
    >
      <p className="text-2xl font-bold text-emerald-600">{value}</p>
      <p className="text-gray-600 font-medium mt-1">{title}</p>
    </Link>
  )
}

function ProcurementActivityCard({
  data,
  range,
  onRangeChange,
}: {
  data: ProcurementActivityPoint[]
  range: '7' | '30'
  onRangeChange: (r: '7' | '30') => void
}) {
  const points = range === '30' ? data : data.slice(-7)
  const maxCount = Math.max(1, ...points.map((p) => p.count))
  const height = 160
  const width = 400
  const padding = { top: 16, right: 16, bottom: 24, left: 32 }
  const chartWidth = width - padding.left - padding.right
  const chartHeight = height - padding.top - padding.bottom

  const pathPoints = points.map((p, i) => {
    const x = padding.left + (i / Math.max(1, points.length - 1)) * chartWidth
    const y = padding.top + chartHeight - (p.count / maxCount) * chartHeight
    return `${x},${y}`
  })
  const pathD = pathPoints.length ? `M ${pathPoints.join(' L ')}` : ''

  return (
    <div className="bg-white rounded-xl border border-gray-100 p-6 shadow-sm">
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-lg font-semibold text-gray-800">Procurement Activity</h2>
        <select
          value={range}
          onChange={(e) => onRangeChange(e.target.value as '7' | '30')}
          className="text-sm border border-gray-200 rounded-lg px-3 py-1.5 text-gray-700 bg-white"
        >
          <option value="7">Last 7 days</option>
          <option value="30">Last 30 days</option>
        </select>
      </div>
      <div className="overflow-x-auto">
        <svg viewBox={`0 0 ${width} ${height}`} className="w-full min-h-[160px]" preserveAspectRatio="xMidYMid meet">
          <path
            d={pathD}
            fill="none"
            stroke="#059669"
            strokeWidth="2"
            strokeLinecap="round"
            strokeLinejoin="round"
          />
        </svg>
      </div>
      {points.length > 0 && (
        <div className="flex justify-between text-xs text-gray-400 mt-1">
          <span>{points[0]?.date ? new Date(points[0].date).toLocaleDateString('en-US', { month: 'short', day: 'numeric' }) : ''}</span>
          <span>{points[points.length - 1]?.date ? new Date(points[points.length - 1].date).toLocaleDateString('en-US', { month: 'short', day: 'numeric' }) : ''}</span>
        </div>
      )}
    </div>
  )
}

function ContractsExpiringCard({ contracts }: { contracts: { id: number; contractNumber?: string; vendorName?: string; endDate?: string }[] }) {
  return (
    <div className="bg-white rounded-xl border border-gray-100 p-6 shadow-sm">
      <h2 className="text-lg font-semibold text-gray-800 mb-4">Contracts expiring in 30 days</h2>
      {contracts.length > 0 ? (
        <ul className="space-y-3">
          {contracts.map((c) => (
            <li key={c.id} className="flex items-center gap-3">
              <span className="text-gray-400">
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </span>
              <Link href={`/contracts/${c.id}`} className="text-emerald-600 hover:underline font-medium">
                {c.vendorName ?? 'Contract'} – {c.endDate ? new Date(c.endDate).toLocaleDateString('en-US', { month: 'short', day: 'numeric' }) : ''}
              </Link>
            </li>
          ))}
        </ul>
      ) : (
        <p className="text-gray-500 text-sm">No contracts expiring in the next 30 days</p>
      )}
    </div>
  )
}

function RecentActivityCard({
  items,
  title = 'Recent Activity',
}: {
  items: { type: string; message: string; timestamp: string; entityType: string; entityId: number }[]
  title?: string
}) {
  return (
    <div className="bg-white rounded-xl border border-gray-100 p-6 shadow-sm">
      <h2 className="text-lg font-semibold text-gray-800 mb-4">{title}</h2>
      {items.length > 0 ? (
        <ul className="space-y-3">
          {items.slice(0, 5).map((item, i) => (
            <li key={`${item.entityType}-${item.entityId}-${i}`} className="flex items-start gap-3">
              <span className="text-gray-400 shrink-0 mt-0.5">
                <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </span>
              <div>
                <p className="text-gray-800 text-sm">{item.message}</p>
                <p className="text-gray-400 text-xs mt-0.5">
                  {item.timestamp ? new Date(item.timestamp).toLocaleString() : ''}
                </p>
              </div>
            </li>
          ))}
        </ul>
      ) : (
        <p className="text-gray-500 text-sm">No recent activity</p>
      )}
    </div>
  )
}
