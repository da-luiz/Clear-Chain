'use client'

import { useEffect, useState } from 'react'
import { useRouter, useParams } from 'next/navigation'
import { getPurchaseOrder, PurchaseOrder } from '@/lib/api'
import Link from 'next/link'

export default function PurchaseOrderDetailPage() {
  const params = useParams()
  const router = useRouter()
  const poId = parseInt(params.id as string)
  const [purchaseOrder, setPurchaseOrder] = useState<PurchaseOrder | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    loadPurchaseOrder()
  }, [poId])

  const loadPurchaseOrder = async () => {
    try {
      setLoading(true)
      setError(null)
      const data = await getPurchaseOrder(poId)
      setPurchaseOrder(data)
    } catch (err) {
      setError('Failed to load purchase order. Make sure the backend is running.')
      console.error('Error loading purchase order:', err)
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="text-lg">Loading purchase order...</div>
      </div>
    )
  }

  if (error || !purchaseOrder) {
    return (
      <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
        <strong className="font-bold">Error: </strong>
        <span>{error || 'Purchase order not found'}</span>
        <div className="mt-4">
          <Link href="/purchase-orders" className="text-blue-600 hover:text-blue-800">
            ← Back to Purchase Orders
          </Link>
        </div>
      </div>
    )
  }

  return (
    <div>
      <div className="mb-6">
        <Link href="/purchase-orders" className="text-blue-600 hover:text-blue-800 mb-4 inline-block">
          ← Back to Purchase Orders
        </Link>
        <div className="flex justify-between items-center">
          <div>
            <h1 className="text-3xl font-bold">Purchase Order #{purchaseOrder.poNumber}</h1>
            <p className="text-gray-600 mt-1">{purchaseOrder.description}</p>
          </div>
          <span className={`px-4 py-2 rounded-full text-sm font-semibold ${
            purchaseOrder.status === 'APPROVED' || purchaseOrder.status === 'RECEIVED' ? 'bg-green-100 text-green-800' :
            purchaseOrder.status === 'PENDING_APPROVAL' ? 'bg-yellow-100 text-yellow-800' :
            purchaseOrder.status === 'DRAFT' ? 'bg-gray-100 text-gray-800' :
            purchaseOrder.status === 'REJECTED' ? 'bg-red-100 text-red-800' :
            'bg-blue-100 text-blue-800'
          }`}>
            {purchaseOrder.status}
          </span>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-semibold mb-4">Purchase Order Details</h2>
          <dl className="space-y-3">
            <div>
              <dt className="text-sm font-medium text-gray-500">PO Number</dt>
              <dd className="mt-1 text-sm text-gray-900">{purchaseOrder.poNumber}</dd>
            </div>
            <div>
              <dt className="text-sm font-medium text-gray-500">Description</dt>
              <dd className="mt-1 text-sm text-gray-900">{purchaseOrder.description}</dd>
            </div>
            <div>
              <dt className="text-sm font-medium text-gray-500">Total Amount</dt>
              <dd className="mt-1 text-sm text-gray-900 font-semibold">
                {purchaseOrder.currency ? `${purchaseOrder.currency} ${purchaseOrder.totalAmount.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}` : purchaseOrder.totalAmount.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
              </dd>
            </div>
            <div>
              <dt className="text-sm font-medium text-gray-500">Order Date</dt>
              <dd className="mt-1 text-sm text-gray-900">{new Date(purchaseOrder.orderDate).toLocaleDateString()}</dd>
            </div>
            {purchaseOrder.expectedDeliveryDate && (
              <div>
                <dt className="text-sm font-medium text-gray-500">Expected Delivery Date</dt>
                <dd className="mt-1 text-sm text-gray-900">{new Date(purchaseOrder.expectedDeliveryDate).toLocaleDateString()}</dd>
              </div>
            )}
          </dl>
        </div>

        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-semibold mb-4">Vendor Information</h2>
          <dl className="space-y-3">
            <div>
              <dt className="text-sm font-medium text-gray-500">Vendor</dt>
              <dd className="mt-1 text-sm text-gray-900">
                <Link href={`/vendors/${purchaseOrder.vendorId}`} className="text-blue-600 hover:text-blue-800">
                  {purchaseOrder.vendorName}
                </Link>
              </dd>
            </div>
            {purchaseOrder.deliveryAddress && (
              <div>
                <dt className="text-sm font-medium text-gray-500">Delivery Address</dt>
                <dd className="mt-1 text-sm text-gray-900">{purchaseOrder.deliveryAddress}</dd>
              </div>
            )}
            {purchaseOrder.paymentTerms && (
              <div>
                <dt className="text-sm font-medium text-gray-500">Payment Terms</dt>
                <dd className="mt-1 text-sm text-gray-900">{purchaseOrder.paymentTerms}</dd>
              </div>
            )}
          </dl>
        </div>

        {purchaseOrder.notes && (
          <div className="bg-white rounded-lg shadow p-6 md:col-span-2">
            <h2 className="text-xl font-semibold mb-4">Notes</h2>
            <p className="text-sm text-gray-900 whitespace-pre-wrap">{purchaseOrder.notes}</p>
          </div>
        )}

        {purchaseOrder.rejectionReason && (
          <div className="bg-red-50 border-l-4 border-red-400 p-6 md:col-span-2">
            <h2 className="text-xl font-semibold mb-4 text-red-800">Rejection Reason</h2>
            <p className="text-sm text-red-900 whitespace-pre-wrap">{purchaseOrder.rejectionReason}</p>
          </div>
        )}
      </div>
    </div>
  )
}

