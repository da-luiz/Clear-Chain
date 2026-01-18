'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import Link from 'next/link'
import { getDashboard, DashboardSummary } from '@/lib/api'
import { getErrorMessage } from '@/lib/errorHandler'

export default function Dashboard() {
  const [dashboard, setDashboard] = useState<DashboardSummary | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    loadDashboard()
  }, [])

  const loadDashboard = async () => {
    try {
      setLoading(true)
      setError(null)
      const data = await getDashboard()
      setDashboard(data)
    } catch (err: any) {
      setError(getErrorMessage(err) || 'Failed to load dashboard. Make sure the backend is running on http://localhost:8080')
      console.error('Error loading dashboard:', err)
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="text-lg">Loading dashboard...</div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
        <strong className="font-bold">Error: </strong>
        <span className="block sm:inline">{error}</span>
      </div>
    )
  }

  if (!dashboard) {
    return <div>No data available</div>
  }

  const formatCurrency = (amount: number | undefined) => {
    if (!amount) return '$0'
    if (amount >= 1000000) {
      return `$${(amount / 1000000).toFixed(2)}M`
    } else if (amount >= 1000) {
      return `$${(amount / 1000).toFixed(1)}K`
    }
    return `$${amount.toFixed(0)}`
  }

  return (
    <div>
      <h1 className="text-3xl font-bold mb-6">Dashboard</h1>
      
      {/* Top Row - Main Statistics Cards - Clickable */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        <Link href="/vendors" className="bg-white p-6 rounded-lg shadow hover:shadow-xl transition-all transform hover:scale-105 cursor-pointer border-l-4 border-blue-500">
          <h2 className="text-3xl font-bold text-blue-600">{dashboard.totalVendors}</h2>
          <p className="text-gray-600 font-medium mt-1">Total Vendors</p>
          <div className="mt-3 text-sm text-gray-600 space-y-1">
            <div className="flex justify-between">
              <span>Active:</span>
              <span className="font-semibold text-green-600">{dashboard.activeVendors}</span>
            </div>
            <div className="flex justify-between">
              <span>Pending:</span>
              <span className="font-semibold text-yellow-600">{dashboard.pendingVendors}</span>
            </div>
            <div className="flex justify-between">
              <span>Inactive:</span>
              <span className="font-semibold text-gray-500">{dashboard.inactiveVendors}</span>
            </div>
            {dashboard.suspendedVendors > 0 && (
              <div className="flex justify-between">
                <span>Suspended:</span>
                <span className="font-semibold text-red-600">{dashboard.suspendedVendors}</span>
              </div>
            )}
          </div>
          <p className="mt-4 text-xs text-blue-500 font-medium">Click to view all vendors →</p>
        </Link>
        
        <Link href="/users" className="bg-white p-6 rounded-lg shadow hover:shadow-xl transition-all transform hover:scale-105 cursor-pointer border-l-4 border-green-500">
          <h2 className="text-3xl font-bold text-green-600">{dashboard.totalActiveUsers}</h2>
          <p className="text-gray-600 font-medium mt-1">Active Users</p>
          <p className="mt-6 text-xs text-green-500 font-medium">Click to manage users →</p>
        </Link>
        
        <Link href="/vendor-requests" className="bg-white p-6 rounded-lg shadow hover:shadow-xl transition-all transform hover:scale-105 cursor-pointer border-l-4 border-orange-500">
          <h2 className="text-3xl font-bold text-orange-600">{dashboard.pendingVendorRequests}</h2>
          <p className="text-gray-600 font-medium mt-1">Pending Requests</p>
          <div className="mt-3 text-sm text-gray-600 space-y-1">
            <div className="flex justify-between">
              <span>Submitted:</span>
              <span className="font-semibold">{dashboard.submittedRequests}</span>
            </div>
            <div className="flex justify-between">
              <span>Under Review:</span>
              <span className="font-semibold">{dashboard.underReviewRequests}</span>
            </div>
          </div>
          <p className="mt-4 text-xs text-orange-500 font-medium">Click to view requests →</p>
        </Link>
        
        <Link href="/purchase-orders" className="bg-white p-6 rounded-lg shadow hover:shadow-xl transition-all transform hover:scale-105 cursor-pointer border-l-4 border-purple-500">
          <h2 className="text-3xl font-bold text-purple-600">{dashboard.totalPurchaseOrders}</h2>
          <p className="text-gray-600 font-medium mt-1">Purchase Orders</p>
          <p className="mt-6 text-xs text-purple-500 font-medium">Click to view orders →</p>
        </Link>
      </div>
      
      {/* Second Row - Real Financial Metrics */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        <Link href="/contracts" className="bg-white p-6 rounded-lg shadow hover:shadow-xl transition-all transform hover:scale-105 cursor-pointer border-l-4 border-indigo-500">
          <h2 className="text-3xl font-bold text-indigo-600">{dashboard.totalContracts}</h2>
          <p className="text-gray-600 font-medium mt-1">Total Contracts</p>
          {dashboard.totalContractValue !== undefined && dashboard.totalContractValue > 0 && (
            <div className="mt-2 text-sm text-gray-600">
              <div className="flex justify-between">
                <span>Total Value:</span>
                <span className="font-semibold text-indigo-600">{formatCurrency(dashboard.totalContractValue)}</span>
              </div>
            </div>
          )}
          <p className="mt-4 text-xs text-indigo-500 font-medium">Click to view contracts →</p>
        </Link>

        <Link href="/purchase-orders" className="bg-white p-6 rounded-lg shadow hover:shadow-xl transition-all transform hover:scale-105 cursor-pointer border-l-4 border-teal-500">
          <h2 className="text-2xl font-bold text-teal-600">{formatCurrency(dashboard.totalPurchaseOrderSpend || 0)}</h2>
          <p className="text-gray-600 font-medium mt-1">Purchase Order Spend</p>
          {dashboard.totalSpendYtd !== undefined && dashboard.totalSpendYtd > 0 && (
            <div className="mt-2 text-sm text-gray-600">
              <div className="flex justify-between">
                <span>YTD:</span>
                <span className="font-semibold">{formatCurrency(dashboard.totalSpendYtd)}</span>
              </div>
            </div>
          )}
          <p className="mt-4 text-xs text-teal-500 font-medium">Click to view orders →</p>
        </Link>

        <Link href="/purchase-orders" className="bg-white p-6 rounded-lg shadow hover:shadow-xl transition-all transform hover:scale-105 cursor-pointer border-l-4 border-blue-500">
          <h2 className="text-3xl font-bold text-blue-600">{dashboard.totalPurchaseOrders}</h2>
          <p className="text-gray-600 font-medium mt-1">Total Purchase Orders</p>
          <p className="mt-6 text-xs text-blue-500 font-medium">Click to view all →</p>
        </Link>

        {dashboard.pendingApprovalsCount !== undefined && dashboard.pendingApprovalsCount > 0 && (
          <Link href="/vendor-requests" className="bg-white p-6 rounded-lg shadow hover:shadow-xl transition-all transform hover:scale-105 cursor-pointer border-l-4 border-red-500 relative">
            <div className="absolute top-4 right-4 bg-red-500 text-white rounded-full w-8 h-8 flex items-center justify-center text-sm font-bold">
              {dashboard.pendingApprovalsCount}
            </div>
            <h2 className="text-3xl font-bold text-red-600">{dashboard.pendingApprovalsCount}</h2>
            <p className="text-gray-600 font-medium mt-1">Pending Approvals</p>
            <p className="mt-6 text-xs text-red-500 font-medium">Click to review →</p>
          </Link>
        )}
      </div>

      {/* Latest Vendors */}
      <div className="bg-white rounded-lg shadow p-6 mb-6">
        <h2 className="text-2xl font-bold mb-4">Latest Vendors</h2>
        {dashboard.latestVendors && dashboard.latestVendors.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Vendor Code
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Company Name
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Status
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Category
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {dashboard.latestVendors.map((vendor) => (
                  <tr key={vendor.id}>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                      {vendor.vendorCode}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {vendor.companyName}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                        vendor.status === 'ACTIVE' ? 'bg-green-100 text-green-800' :
                        vendor.status === 'PENDING_CREATION' ? 'bg-yellow-100 text-yellow-800' :
                        'bg-gray-100 text-gray-800'
                      }`}>
                        {vendor.status}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {vendor.categoryName || '-'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <p className="text-gray-500">No vendors yet. Create your first vendor to get started.</p>
        )}
      </div>

      {/* Pending Approvals / Reviews */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-6">
        {/* Pending Purchase Orders */}
        <div className="bg-white rounded-lg shadow p-6">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-xl font-bold">Pending Approval Purchase Orders</h2>
            <Link href="/purchase-orders" className="text-sm text-blue-600 hover:text-blue-800">View All →</Link>
          </div>
          {dashboard.pendingApprovalPurchaseOrders && dashboard.pendingApprovalPurchaseOrders.length > 0 ? (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">PO Number</th>
                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">Vendor</th>
                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">Amount</th>
                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {dashboard.pendingApprovalPurchaseOrders.map((po) => (
                    <tr key={po.id} className="hover:bg-gray-50">
                      <td className="px-4 py-2 text-sm font-medium text-gray-900">
                        <Link href={`/purchase-orders/${po.id}`} className="text-blue-600 hover:text-blue-800">
                          {po.poNumber}
                        </Link>
                      </td>
                      <td className="px-4 py-2 text-sm text-gray-500">{po.vendorName}</td>
                      <td className="px-4 py-2 text-sm text-gray-500">${po.totalAmount?.toLocaleString()}</td>
                      <td className="px-4 py-2 text-sm">
                        <span className="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-yellow-100 text-yellow-800">
                          {po.status}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <p className="text-gray-500 text-sm">No pending purchase orders</p>
          )}
        </div>

        {/* Expiring Contracts */}
        <div className="bg-white rounded-lg shadow p-6">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-xl font-bold">Expiring Contracts (Next 90 Days)</h2>
            <Link href="/contracts" className="text-sm text-blue-600 hover:text-blue-800">View All →</Link>
          </div>
          {dashboard.expiringContracts && dashboard.expiringContracts.length > 0 ? (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">Contract</th>
                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">Vendor</th>
                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">End Date</th>
                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">Value</th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {dashboard.expiringContracts.map((contract) => (
                    <tr key={contract.id} className="hover:bg-gray-50">
                      <td className="px-4 py-2 text-sm font-medium text-gray-900">
                        <Link href={`/contracts/${contract.id}`} className="text-blue-600 hover:text-blue-800">
                          {contract.contractNumber}
                        </Link>
                      </td>
                      <td className="px-4 py-2 text-sm text-gray-500">{contract.vendorName}</td>
                      <td className="px-4 py-2 text-sm text-gray-500">
                        {contract.endDate ? new Date(contract.endDate).toLocaleDateString() : '-'}
                      </td>
                      <td className="px-4 py-2 text-sm text-gray-500">
                        {contract.contractValue ? (contract.currency ? `${contract.currency} ${contract.contractValue.toLocaleString()}` : contract.contractValue.toLocaleString()) : '-'}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : (
            <p className="text-gray-500 text-sm">No expiring contracts</p>
          )}
        </div>
      </div>

      {/* Latest Vendor Requests */}
      {dashboard.latestVendorRequests && dashboard.latestVendorRequests.length > 0 && (
        <div className="bg-white rounded-lg shadow p-6 mb-6">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-xl font-bold">Pending Vendor Requests</h2>
            <Link href="/vendor-requests" className="text-sm text-blue-600 hover:text-blue-800">View All →</Link>
          </div>
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">Request Number</th>
                  <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">Company Name</th>
                  <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">Department</th>
                  <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase">Status</th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {dashboard.latestVendorRequests.map((request: any) => (
                  <tr key={request.id} className="hover:bg-gray-50">
                    <td className="px-4 py-2 text-sm font-medium text-gray-900">
                      <Link href={`/vendor-requests/${request.id}`} className="text-blue-600 hover:text-blue-800">
                        {request.requestNumber}
                      </Link>
                    </td>
                    <td className="px-4 py-2 text-sm text-gray-500">{request.companyName}</td>
                    <td className="px-4 py-2 text-sm text-gray-500">{request.requestingDepartmentName || '-'}</td>
                    <td className="px-4 py-2 text-sm">
                      <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                        request.status === 'SUBMITTED' ? 'bg-blue-100 text-blue-800' :
                        request.status === 'UNDER_REVIEW' ? 'bg-yellow-100 text-yellow-800' :
                        'bg-gray-100 text-gray-800'
                      }`}>
                        {request.status}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  )
}

