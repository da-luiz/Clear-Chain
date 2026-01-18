'use client'

import { useState, useEffect, Suspense } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import { createPurchaseOrder, getVendors, getUsers, Vendor, User } from '@/lib/api'
import Link from 'next/link'
import { getErrorMessage } from '@/lib/errorHandler'

function CreatePurchaseOrderForm() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const vendorIdParam = searchParams.get('vendorId')
  
  const [vendors, setVendors] = useState<Vendor[]>([])
  const [users, setUsers] = useState<User[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [formData, setFormData] = useState({
    vendorId: vendorIdParam || '',
    description: '',
    totalAmount: '',
    currency: '',
    orderDate: new Date().toISOString().split('T')[0],
    expectedDeliveryDate: '',
    createdByUserId: '',
    deliveryAddress: '',
    paymentTerms: '',
    notes: '',
  })
  
  // Currency options - comprehensive list
  const currencies = [
    { code: 'NGN', name: 'Nigerian Naira' },
    { code: 'USD', name: 'US Dollar' },
    { code: 'EUR', name: 'Euro' },
    { code: 'GBP', name: 'British Pound' },
    { code: 'JPY', name: 'Japanese Yen' },
    { code: 'AUD', name: 'Australian Dollar' },
    { code: 'CAD', name: 'Canadian Dollar' },
    { code: 'CHF', name: 'Swiss Franc' },
    { code: 'CNY', name: 'Chinese Yuan' },
    { code: 'INR', name: 'Indian Rupee' },
    { code: 'SGD', name: 'Singapore Dollar' },
    { code: 'HKD', name: 'Hong Kong Dollar' },
    { code: 'ZAR', name: 'South African Rand' },
    { code: 'BRL', name: 'Brazilian Real' },
    { code: 'MXN', name: 'Mexican Peso' },
    { code: 'KRW', name: 'South Korean Won' },
    { code: 'TRY', name: 'Turkish Lira' },
    { code: 'RUB', name: 'Russian Ruble' },
    { code: 'AED', name: 'UAE Dirham' },
    { code: 'SAR', name: 'Saudi Riyal' },
  ]

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    try {
      const [vendorsData, usersData] = await Promise.all([
        getVendors(100),
        getUsers()
      ])
      setVendors(vendorsData)
      setUsers(usersData)
      
      // Set default user (first active user)
      const firstUser = usersData.find(u => u.active)
      if (firstUser) {
        setFormData(prev => ({ ...prev, createdByUserId: firstUser.id.toString() }))
      }
    } catch (err) {
      console.error('Error loading data:', err)
    }
  }

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target
    setFormData(prev => ({
      ...prev,
      [name]: value
    }))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setError(null)

    try {
      const poData: any = {
        vendorId: parseInt(formData.vendorId),
        description: formData.description,
        totalAmount: parseFloat(formData.totalAmount),
        currency: formData.currency || undefined,
        orderDate: formData.orderDate,
        createdByUserId: parseInt(formData.createdByUserId),
      }

      if (formData.expectedDeliveryDate) poData.expectedDeliveryDate = formData.expectedDeliveryDate
      if (formData.deliveryAddress) poData.deliveryAddress = formData.deliveryAddress
      if (formData.paymentTerms) poData.paymentTerms = formData.paymentTerms
      if (formData.notes) poData.notes = formData.notes

      await createPurchaseOrder(poData)
      router.push('/purchase-orders')
    } catch (err: any) {
      setError(getErrorMessage(err) || 'Failed to create purchase order')
      console.error('Error creating purchase order:', err)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <div className="mb-6">
        <Link href="/purchase-orders" className="text-blue-600 hover:text-blue-800 mb-4 inline-block">
          ← Back to Purchase Orders
        </Link>
        <h1 className="text-3xl font-bold">Create Purchase Order</h1>
      </div>

      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
          <strong className="font-bold">Error: </strong>
          <span>{error}</span>
        </div>
      )}

      <div className="bg-white rounded-lg shadow p-6">
        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label htmlFor="vendorId" className="block text-sm font-medium text-gray-700 mb-1">
                Vendor <span className="text-red-500">*</span>
              </label>
              <select
                id="vendorId"
                name="vendorId"
                value={formData.vendorId}
                onChange={handleChange}
                required
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
              >
                <option value="">Select a vendor</option>
                {vendors.map(vendor => (
                  <option key={vendor.id} value={vendor.id}>
                    {vendor.companyName} ({vendor.vendorCode})
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label htmlFor="createdByUserId" className="block text-sm font-medium text-gray-700 mb-1">
                Created By <span className="text-red-500">*</span>
              </label>
              <select
                id="createdByUserId"
                name="createdByUserId"
                value={formData.createdByUserId}
                onChange={handleChange}
                required
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
              >
                <option value="">Select a user</option>
                {users.filter(u => u.active).map(user => (
                  <option key={user.id} value={user.id}>
                    {user.firstName} {user.lastName} ({user.username})
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label htmlFor="description" className="block text-sm font-medium text-gray-700 mb-1">
                Description <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                id="description"
                name="description"
                value={formData.description}
                onChange={handleChange}
                required
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                placeholder="Purchase order description"
              />
            </div>

            <div>
              <label htmlFor="totalAmount" className="block text-sm font-medium text-gray-700 mb-1">
                Total Amount <span className="text-red-500">*</span>
              </label>
              <input
                type="number"
                id="totalAmount"
                name="totalAmount"
                value={formData.totalAmount}
                onChange={handleChange}
                required
                min="0.01"
                step="0.01"
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                placeholder="0.00"
              />
            </div>
            
            {formData.totalAmount && (
              <div>
                <label htmlFor="currency" className="block text-sm font-medium text-gray-700 mb-1">
                  Currency
                </label>
                <select
                  id="currency"
                  name="currency"
                  value={formData.currency}
                  onChange={handleChange}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                >
                  <option value="">Select currency</option>
                  {currencies.map(curr => (
                    <option key={curr.code} value={curr.code}>
                      {curr.code} - {curr.name}
                    </option>
                  ))}
                </select>
              </div>
            )}

            <div>
              <label htmlFor="orderDate" className="block text-sm font-medium text-gray-700 mb-1">
                Order Date <span className="text-red-500">*</span>
              </label>
              <input
                type="date"
                id="orderDate"
                name="orderDate"
                value={formData.orderDate}
                onChange={handleChange}
                required
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
              />
            </div>

            <div>
              <label htmlFor="expectedDeliveryDate" className="block text-sm font-medium text-gray-700 mb-1">
                Expected Delivery Date
              </label>
              <input
                type="date"
                id="expectedDeliveryDate"
                name="expectedDeliveryDate"
                value={formData.expectedDeliveryDate}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
              />
            </div>

            <div>
              <label htmlFor="paymentTerms" className="block text-sm font-medium text-gray-700 mb-1">
                Payment Terms
              </label>
              <input
                type="text"
                id="paymentTerms"
                name="paymentTerms"
                value={formData.paymentTerms}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                placeholder="e.g., Net 30"
              />
            </div>
          </div>

          <div>
            <label htmlFor="deliveryAddress" className="block text-sm font-medium text-gray-700 mb-1">
              Delivery Address
            </label>
            <textarea
              id="deliveryAddress"
              name="deliveryAddress"
              value={formData.deliveryAddress}
              onChange={handleChange}
              rows={3}
              className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
              placeholder="Enter delivery address"
            />
          </div>

          <div>
            <label htmlFor="notes" className="block text-sm font-medium text-gray-700 mb-1">
              Notes
            </label>
            <textarea
              id="notes"
              name="notes"
              value={formData.notes}
              onChange={handleChange}
              rows={4}
              className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
              placeholder="Additional notes"
            />
          </div>

          <div className="flex justify-end space-x-4 pt-4 border-t">
            <Link
              href="/purchase-orders"
              className="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50"
            >
              Cancel
            </Link>
            <button
              type="submit"
              disabled={loading}
              className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed"
            >
              {loading ? 'Creating...' : 'Create Purchase Order'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default function CreatePurchaseOrderPage() {
  return (
    <Suspense fallback={<div className="flex justify-center items-center h-64">Loading...</div>}>
      <CreatePurchaseOrderForm />
    </Suspense>
  )
}



