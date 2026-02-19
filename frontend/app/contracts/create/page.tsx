'use client'

import { useState, useEffect, Suspense } from 'react'
import { useRouter, useSearchParams } from 'next/navigation'
import { createContract, getVendors, getUsers, Vendor, User } from '@/lib/api'
import Link from 'next/link'
import { getErrorMessage } from '@/lib/errorHandler'

function CreateContractForm() {
  const router = useRouter()
  const searchParams = useSearchParams()
  const vendorIdParam = searchParams.get('vendorId')
  
  const [vendors, setVendors] = useState<Vendor[]>([])
  const [users, setUsers] = useState<User[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [formData, setFormData] = useState({
    vendorId: vendorIdParam || '',
    contractNumber: '',
    title: '',
    description: '',
    contractValue: '',
    currency: '',
    startDate: new Date().toISOString().split('T')[0],
    endDate: '',
    contractType: '',
    termsAndConditions: '',
    createdByUserId: '',
    renewalTerms: '',
    terminationClause: '',
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
      const contractData: any = {
        vendorId: parseInt(formData.vendorId),
        contractNumber: formData.contractNumber,
        title: formData.title,
        startDate: formData.startDate,
        endDate: formData.endDate,
        createdByUserId: parseInt(formData.createdByUserId),
      }

      if (formData.description) contractData.description = formData.description
      if (formData.contractValue) contractData.contractValue = parseFloat(formData.contractValue)
      if (formData.currency) contractData.currency = formData.currency
      if (formData.contractType) contractData.contractType = formData.contractType
      if (formData.termsAndConditions) contractData.termsAndConditions = formData.termsAndConditions
      if (formData.renewalTerms) contractData.renewalTerms = formData.renewalTerms
      if (formData.terminationClause) contractData.terminationClause = formData.terminationClause

      await createContract(contractData)
      router.push('/contracts')
    } catch (err: any) {
      setError(getErrorMessage(err) || 'Failed to create contract')
      console.error('Error creating contract:', err)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <div className="mb-6">
        <Link href="/contracts" className="text-blue-600 hover:text-blue-800 mb-4 inline-block">
          ← Back to Contracts
        </Link>
        <h1 className="text-3xl font-bold">Create Contract</h1>
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
              <label htmlFor="contractNumber" className="block text-sm font-medium text-gray-700 mb-1">
                Contract Number <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                id="contractNumber"
                name="contractNumber"
                value={formData.contractNumber}
                onChange={handleChange}
                required
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                placeholder="CONTRACT-001"
              />
            </div>

            <div>
              <label htmlFor="title" className="block text-sm font-medium text-gray-700 mb-1">
                Title <span className="text-red-500">*</span>
              </label>
              <input
                type="text"
                id="title"
                name="title"
                value={formData.title}
                onChange={handleChange}
                required
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                placeholder="Contract title"
              />
            </div>

            <div>
              <label htmlFor="contractValue" className="block text-sm font-medium text-gray-700 mb-1">
                Contract Value
              </label>
              <input
                type="number"
                id="contractValue"
                name="contractValue"
                value={formData.contractValue}
                onChange={handleChange}
                min="0"
                step="0.01"
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                placeholder="0.00"
              />
            </div>
            
            {formData.contractValue && (
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
              <label htmlFor="contractType" className="block text-sm font-medium text-gray-700 mb-1">
                Contract Type
              </label>
              <select
                id="contractType"
                name="contractType"
                value={formData.contractType}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
              >
                <option value="">Select type</option>
                <option value="SERVICE">Service</option>
                <option value="SUPPLY">Supply</option>
                <option value="MAINTENANCE">Maintenance</option>
                <option value="CONSULTING">Consulting</option>
              </select>
            </div>

            <div>
              <label htmlFor="startDate" className="block text-sm font-medium text-gray-700 mb-1">
                Start Date <span className="text-red-500">*</span>
              </label>
              <input
                type="date"
                id="startDate"
                name="startDate"
                value={formData.startDate}
                onChange={handleChange}
                required
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
              />
            </div>

            <div>
              <label htmlFor="endDate" className="block text-sm font-medium text-gray-700 mb-1">
                End Date <span className="text-red-500">*</span>
              </label>
              <input
                type="date"
                id="endDate"
                name="endDate"
                value={formData.endDate}
                onChange={handleChange}
                required
                className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
              />
            </div>
          </div>

          <div>
            <label htmlFor="description" className="block text-sm font-medium text-gray-700 mb-1">
              Description
            </label>
            <textarea
              id="description"
              name="description"
              value={formData.description}
              onChange={handleChange}
              rows={4}
              className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
              placeholder="Contract description"
            />
          </div>

          <div>
            <label htmlFor="termsAndConditions" className="block text-sm font-medium text-gray-700 mb-1">
              Terms and Conditions
            </label>
            <textarea
              id="termsAndConditions"
              name="termsAndConditions"
              value={formData.termsAndConditions}
              onChange={handleChange}
              rows={4}
              className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
              placeholder="Terms and conditions"
            />
          </div>

          <div>
            <label htmlFor="renewalTerms" className="block text-sm font-medium text-gray-700 mb-1">
              Renewal Terms
            </label>
            <textarea
              id="renewalTerms"
              name="renewalTerms"
              value={formData.renewalTerms}
              onChange={handleChange}
              rows={3}
              className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
              placeholder="Renewal terms"
            />
          </div>

          <div>
            <label htmlFor="terminationClause" className="block text-sm font-medium text-gray-700 mb-1">
              Termination Clause
            </label>
            <textarea
              id="terminationClause"
              name="terminationClause"
              value={formData.terminationClause}
              onChange={handleChange}
              rows={3}
              className="w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
              placeholder="Termination clause"
            />
          </div>

          <div className="flex justify-end space-x-4 pt-4 border-t">
            <Link
              href="/contracts"
              className="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50"
            >
              Cancel
            </Link>
            <button
              type="submit"
              disabled={loading}
              className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed"
            >
              {loading ? 'Creating...' : 'Create Contract'}
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default function CreateContractPage() {
  return (
    <Suspense fallback={<div className="flex justify-center items-center h-64">Loading...</div>}>
      <CreateContractForm />
    </Suspense>
  )
}



