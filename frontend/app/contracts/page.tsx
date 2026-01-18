'use client'

import { useEffect, useState } from 'react'
import { getContractsForVendor, getVendors, Contract, Vendor } from '@/lib/api'
import Link from 'next/link'

export default function ContractsPage() {
  const [allContracts, setAllContracts] = useState<Contract[]>([])
  const [vendors, setVendors] = useState<Vendor[]>([])
  const [selectedVendorId, setSelectedVendorId] = useState<string>('all')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    loadVendors()
  }, [])

  useEffect(() => {
    if (vendors.length > 0) {
      loadContracts()
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

  const loadContracts = async () => {
    try {
      setLoading(true)
      setError(null)
      
      if (selectedVendorId === 'all') {
        const allContractsList: Contract[] = []
        for (const vendor of vendors) {
          try {
            const contracts = await getContractsForVendor(vendor.id)
            allContractsList.push(...contracts)
          } catch (err) {
            // Skip vendors with no contracts
          }
        }
        setAllContracts(allContractsList)
      } else {
        const data = await getContractsForVendor(parseInt(selectedVendorId))
        setAllContracts(data)
      }
    } catch (err) {
      setError('Failed to load contracts. Make sure the backend is running.')
      console.error('Error loading contracts:', err)
    } finally {
      setLoading(false)
    }
  }

  if (loading && allContracts.length === 0) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="text-lg">Loading contracts...</div>
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
        <h1 className="text-3xl font-bold">Contracts</h1>
        <Link
          href="/contracts/create"
          className="bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded"
        >
          + Create Contract
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

      {allContracts.length > 0 ? (
        <div className="bg-white rounded-lg shadow overflow-hidden">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Contract Number
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Title
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Vendor
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Value
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Status
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Start Date
                </th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  End Date
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {allContracts.map((contract) => (
                <tr key={contract.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                    <Link href={`/contracts/${contract.id}`} className="text-blue-600 hover:text-blue-800">
                      {contract.contractNumber}
                    </Link>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    {contract.title}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {contract.vendorName}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                    {contract.contractValue 
                      ? contract.currency 
                        ? `${contract.currency} ${contract.contractValue.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
                        : contract.contractValue.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
                      : '-'}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${
                      contract.status === 'ACTIVE' ? 'bg-green-100 text-green-800' :
                      contract.status === 'DRAFT' ? 'bg-gray-100 text-gray-800' :
                      contract.status === 'TERMINATED' ? 'bg-red-100 text-red-800' :
                      contract.status === 'EXPIRED' ? 'bg-yellow-100 text-yellow-800' :
                      'bg-blue-100 text-blue-800'
                    }`}>
                      {contract.status}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {new Date(contract.startDate).toLocaleDateString()}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {new Date(contract.endDate).toLocaleDateString()}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <div className="bg-white rounded-lg shadow p-8 text-center">
          <p className="text-gray-500 text-lg mb-4">No contracts found.</p>
          <Link
            href="/contracts/create"
            className="inline-block bg-blue-600 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded"
          >
            + Create Your First Contract
          </Link>
        </div>
      )}
    </div>
  )
}
