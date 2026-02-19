'use client'

import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import { getPurchaseOrdersForVendor, getVendors, PurchaseOrder, Vendor } from '@/lib/api'
import Link from 'next/link'

export default function PurchaseOrdersPage() {
  const router = useRouter()
  const [allPurchaseOrders, setAllPurchaseOrders] = useState<PurchaseOrder[]>([])
  const [vendors, setVendors] = useState<Vendor[]>([])
  const [selectedVendorId, setSelectedVendorId] = useState<string>('all')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    loadVendors()
  }, [])

  useEffect(() => {
    if (vendors.length > 0) {
      loadPurchaseOrders()
    }
  }, [selectedVendorId, vendors])

  const loadVendors = async () => {
    try {
      const data = await getVendors(100)
      setVendors(data)
    } catch (err) {
      console.error('Error loading vendors:', err)
    }
  }

  const loadPurchaseOrders = async () => {
    try {
      setLoading(true)
      setError(null)
      
      if (selectedVendorId === 'all') {
        // Load POs for all vendors
        const allPOs: PurchaseOrder[] = []
        for (const vendor of vendors) {
          try {
            const pos = await getPurchaseOrdersForVendor(vendor.id)
            allPOs.push(...pos)
          } catch (err) {
            // Skip vendors with no POs
          }
        }
        setAllPurchaseOrders(allPOs)
      } else {
        const data = await getPurchaseOrdersForVendor(parseInt(selectedVendorId))
        setAllPurchaseOrders(data)
      }
    } catch (err) {
      setError('Failed to load purchase orders. Make sure the backend is running.')
      console.error('Error loading purchase orders:', err)
    } finally {
      setLoading(false)
    }
  }

  if (loading && allPurchaseOrders.length === 0) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="text-lg">Loading purchase orders...</div>
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
        <h1 className="text-3xl font-bold">Purchase Orders</h1>
        <Link
          href="/purchase-orders/create"
          className="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded"
        >
          + Create Purchase Order
        </Link>
      </div>

      {/* Filter by Vendor */}
      <div className="mb-4">
        <label htmlFor="vendorFilter" className="block text-sm font-medium text-gray-700 mb-2">
          Filter by Vendor
        </label>
        <select
          id="vendorFilter"
          value={selectedVendorId}
          onChange={(e) => setSelectedVendorId(e.target.value)}
          className="px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
        >
          <option value="all">All Vendors</option>
          {vendors.map(vendor => (
            <option key={vendor.id} value={vendor.id}>
              {vendor.companyName}
            </option>
          ))}
        </select>
      </div>

      {allPurchaseOrders.length > 0 ? (
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  PO Number
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Vendor
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Description
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Amount
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Status
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Order Date
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {allPurchaseOrders.map((po) => (
                <tr key={po.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                    <Link href={`/purchase-orders/${po.id}`} className="text-blue-600 hover:text-blue-800">
                      {po.poNumber}
                    </Link>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {po.vendorName}
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-500">
                    {po.description}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    {po.currency ? `${po.currency} ${po.totalAmount.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}` : po.totalAmount.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                      po.status === 'APPROVED' || po.status === 'RECEIVED' ? 'bg-green-100 text-green-800' :
                      po.status === 'PENDING_APPROVAL' ? 'bg-yellow-100 text-yellow-800' :
                      po.status === 'DRAFT' ? 'bg-gray-100 text-gray-800' :
                      po.status === 'REJECTED' ? 'bg-red-100 text-red-800' :
                      'bg-blue-100 text-blue-800'
                    }`}>
                      {po.status}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {new Date(po.orderDate).toLocaleDateString()}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <div className="bg-white rounded-lg shadow p-8 text-center">
          <p className="text-gray-500 text-lg mb-4">No purchase orders found.</p>
          <Link
            href="/purchase-orders/create"
            className="inline-block bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded"
          >
            + Create Your First Purchase Order
          </Link>
        </div>
      )}
    </div>
  )
}
