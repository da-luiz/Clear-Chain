'use client'

import { useEffect, useState } from 'react'
import { useRouter, useParams } from 'next/navigation'
import { getVendor, Vendor, activateVendor, suspendVendor, terminateVendor } from '@/lib/api'
import Link from 'next/link'

export default function VendorDetailPage() {
  const params = useParams()
  const router = useRouter()
  const vendorId = parseInt(params.id as string)
  const [vendor, setVendor] = useState<Vendor | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [actionLoading, setActionLoading] = useState(false)

  useEffect(() => {
    loadVendor()
  }, [vendorId])

  const loadVendor = async () => {
    try {
      setLoading(true)
      setError(null)
      const data = await getVendor(vendorId)
      setVendor(data)
    } catch (err) {
      setError('Failed to load vendor. Make sure the backend is running.')
      console.error('Error loading vendor:', err)
    } finally {
      setLoading(false)
    }
  }

  const handleAction = async (action: 'activate' | 'suspend' | 'terminate') => {
    if (!confirm(`Are you sure you want to ${action} this vendor?`)) return

    try {
      setActionLoading(true)
      if (action === 'activate') await activateVendor(vendorId)
      else if (action === 'suspend') await suspendVendor(vendorId)
      else if (action === 'terminate') await terminateVendor(vendorId)
      
      // Reload vendor data
      await loadVendor()
    } catch (err) {
      alert(`Failed to ${action} vendor`)
      console.error(`Error ${action}ing vendor:`, err)
    } finally {
      setActionLoading(false)
    }
  }

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="text-lg">Loading vendor...</div>
      </div>
    )
  }

  if (error || !vendor) {
    return (
      <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
        <strong className="font-bold">Error: </strong>
        <span>{error || 'Vendor not found'}</span>
        <div className="mt-4">
          <Link href="/vendors" className="text-blue-600 hover:text-blue-800">
            ← Back to Vendors
          </Link>
        </div>
      </div>
    )
  }

  return (
    <div>
      <div className="mb-6">
        <Link href="/vendors" className="text-blue-600 hover:text-blue-800 mb-4 inline-block">
          ← Back to Vendors
        </Link>
        <div className="flex justify-between items-center">
          <h1 className="text-3xl font-bold">{vendor.companyName}</h1>
          <div className="flex space-x-3">
            <Link
              href={`/vendors/${vendorId}/edit`}
              className="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded"
            >
              Edit
            </Link>
            {vendor.status === 'PENDING_CREATION' || vendor.status === 'SUSPENDED' ? (
              <button
                onClick={() => handleAction('activate')}
                disabled={actionLoading}
                className="bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-4 rounded disabled:bg-gray-400"
              >
                Activate
              </button>
            ) : null}
            {vendor.status === 'ACTIVE' ? (
              <>
                <button
                  onClick={() => handleAction('suspend')}
                  disabled={actionLoading}
                  className="bg-yellow-600 hover:bg-yellow-700 text-white font-bold py-2 px-4 rounded disabled:bg-gray-400"
                >
                  Suspend
                </button>
                <button
                  onClick={() => handleAction('terminate')}
                  disabled={actionLoading}
                  className="bg-red-600 hover:bg-red-700 text-white font-bold py-2 px-4 rounded disabled:bg-gray-400"
                >
                  Terminate
                </button>
              </>
            ) : null}
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Basic Information */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-bold mb-4">Basic Information</h2>
          <dl className="space-y-3">
            <div>
              <dt className="text-sm font-medium text-gray-500">Vendor Code</dt>
              <dd className="mt-1 text-sm text-gray-900">{vendor.vendorCode}</dd>
            </div>
            <div>
              <dt className="text-sm font-medium text-gray-500">Company Name</dt>
              <dd className="mt-1 text-sm text-gray-900">{vendor.companyName}</dd>
            </div>
            <div>
              <dt className="text-sm font-medium text-gray-500">Email</dt>
              <dd className="mt-1 text-sm text-gray-900">{vendor.email || '-'}</dd>
            </div>
            <div>
              <dt className="text-sm font-medium text-gray-500">Phone</dt>
              <dd className="mt-1 text-sm text-gray-900">{vendor.phone || '-'}</dd>
            </div>
            <div>
              <dt className="text-sm font-medium text-gray-500">Website</dt>
              <dd className="mt-1 text-sm text-gray-900">
                {vendor.website ? (
                  <a href={vendor.website} target="_blank" rel="noopener noreferrer" className="text-blue-600 hover:underline">
                    {vendor.website}
                  </a>
                ) : '-'}
              </dd>
            </div>
            <div>
              <dt className="text-sm font-medium text-gray-500">Status</dt>
              <dd className="mt-1">
                <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                  vendor.status === 'ACTIVE' ? 'bg-green-100 text-green-800' :
                  vendor.status === 'PENDING_CREATION' ? 'bg-yellow-100 text-yellow-800' :
                  vendor.status === 'SUSPENDED' ? 'bg-red-100 text-red-800' :
                  'bg-gray-100 text-gray-800'
                }`}>
                  {vendor.status}
                </span>
              </dd>
            </div>
            <div>
              <dt className="text-sm font-medium text-gray-500">Category</dt>
              <dd className="mt-1 text-sm text-gray-900">{vendor.categoryName || '-'}</dd>
            </div>
          </dl>
        </div>

        {/* Address */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-bold mb-4">Address</h2>
          {(vendor.street || vendor.city || vendor.country) ? (
            <div className="space-y-2 text-sm text-gray-900">
              {vendor.street && <div>{vendor.street}</div>}
              <div>
                {[vendor.city, vendor.state, vendor.postalCode].filter(Boolean).join(', ')}
              </div>
              {vendor.country && <div>{vendor.country}</div>}
            </div>
          ) : (
            <p className="text-gray-500 text-sm">No address provided</p>
          )}
        </div>

        {/* Description */}
        {vendor.description && (
          <div className="bg-white rounded-lg shadow p-6 md:col-span-2">
            <h2 className="text-xl font-bold mb-4">Description</h2>
            <p className="text-sm text-gray-900 whitespace-pre-wrap">{vendor.description}</p>
          </div>
        )}
      </div>

      {/* Actions */}
      <div className="mt-6 bg-white rounded-lg shadow p-6">
        <h2 className="text-xl font-bold mb-4">Quick Actions</h2>
        <div className="flex space-x-3">
          <Link
            href={`/purchase-orders/create?vendorId=${vendorId}`}
            className="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded"
          >
            Create Purchase Order
          </Link>
          <Link
            href={`/contracts/create?vendorId=${vendorId}`}
            className="bg-green-600 hover:bg-green-700 text-white font-bold py-2 px-4 rounded"
          >
            Create Contract
          </Link>
        </div>
      </div>
    </div>
  )
}





