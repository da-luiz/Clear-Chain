'use client'

import { useEffect, useState } from 'react'
import { getVendorRequests, VendorCreationRequest } from '@/lib/api'
import Link from 'next/link'

export default function VendorRequestsPage() {
  const [requests, setRequests] = useState<VendorCreationRequest[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    loadRequests()
  }, [])

  const loadRequests = async () => {
    try {
      setLoading(true)
      setError(null)
      const data = await getVendorRequests()
      setRequests(data)
    } catch (err) {
      setError('Failed to load vendor requests. Make sure the backend is running.')
      console.error('Error loading vendor requests:', err)
    } finally {
      setLoading(false)
    }
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'DRAFT': return 'bg-gray-100 text-gray-800'
      case 'PENDING_COMPLIANCE_REVIEW': return 'bg-yellow-100 text-yellow-800'
      case 'PENDING_FINANCE_REVIEW': return 'bg-blue-100 text-blue-800'
      case 'PENDING_ADMIN_REVIEW': return 'bg-purple-100 text-purple-800'
      case 'ACTIVE': return 'bg-green-100 text-green-800'
      case 'REJECTED_BY_COMPLIANCE': return 'bg-red-100 text-red-800'
      case 'REJECTED_BY_FINANCE': return 'bg-red-100 text-red-800'
      case 'REJECTED_BY_ADMIN': return 'bg-red-100 text-red-800'
      case 'CANCELLED': return 'bg-gray-100 text-gray-800'
      default: return 'bg-gray-100 text-gray-800'
    }
  }

  const formatStatus = (status: string) => {
    return status.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase())
  }

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="text-lg">Loading vendor requests...</div>
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

  return (
    <div>
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold">Vendor Requests</h1>
        <div className="flex space-x-3">
          <button
            onClick={loadRequests}
            className="bg-gray-600 hover:bg-gray-700 text-white font-bold py-2 px-4 rounded"
          >
            Refresh
          </button>
          <Link
            href="/vendor-requests/create"
            className="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded"
          >
            + Create Request
          </Link>
        </div>
      </div>

      {requests.length > 0 ? (
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Request Number
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Company Name
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Department
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Status
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Created
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {requests.map((request) => (
                <tr key={request.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                    <Link href={`/vendor-requests/${request.id}`} className="text-blue-600 hover:text-blue-800">
                      {request.requestNumber}
                    </Link>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    {request.companyName}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {request.requestingDepartmentName || '-'}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${getStatusColor(request.status)}`}>
                      {formatStatus(request.status)}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {request.createdAt ? new Date(request.createdAt).toLocaleDateString() : '-'}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <div className="bg-white rounded-lg shadow p-8 text-center">
          <p className="text-gray-500 text-lg mb-4">No vendor requests found.</p>
          <Link
            href="/vendor-requests/create"
            className="inline-block bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded"
          >
            + Create Your First Request
          </Link>
        </div>
      )}
    </div>
  )
}




