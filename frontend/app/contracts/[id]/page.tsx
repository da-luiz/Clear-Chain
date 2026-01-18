'use client'

import { useEffect, useState } from 'react'
import { useRouter, useParams } from 'next/navigation'
import { getContract, Contract } from '@/lib/api'
import Link from 'next/link'

export default function ContractDetailPage() {
  const params = useParams()
  const router = useRouter()
  const contractId = parseInt(params.id as string)
  const [contract, setContract] = useState<Contract | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    loadContract()
  }, [contractId])

  const loadContract = async () => {
    try {
      setLoading(true)
      setError(null)
      const data = await getContract(contractId)
      setContract(data)
    } catch (err) {
      setError('Failed to load contract. Make sure the backend is running.')
      console.error('Error loading contract:', err)
    } finally {
      setLoading(false)
    }
  }

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="text-lg">Loading contract...</div>
      </div>
    )
  }

  if (error || !contract) {
    return (
      <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
        <strong className="font-bold">Error: </strong>
        <span>{error || 'Contract not found'}</span>
        <div className="mt-4">
          <Link href="/contracts" className="text-blue-600 hover:text-blue-800">
            ← Back to Contracts
          </Link>
        </div>
      </div>
    )
  }

  return (
    <div>
      <div className="mb-6">
        <Link href="/contracts" className="text-blue-600 hover:text-blue-800 mb-4 inline-block">
          ← Back to Contracts
        </Link>
        <div className="flex justify-between items-center">
          <div>
            <h1 className="text-3xl font-bold">{contract.title}</h1>
            <p className="text-gray-600 mt-1">Contract #{contract.contractNumber}</p>
          </div>
          <span className={`px-4 py-2 rounded-full text-sm font-semibold ${
            contract.status === 'ACTIVE' ? 'bg-green-100 text-green-800' :
            contract.status === 'DRAFT' ? 'bg-gray-100 text-gray-800' :
            contract.status === 'TERMINATED' ? 'bg-red-100 text-red-800' :
            contract.status === 'EXPIRED' ? 'bg-yellow-100 text-yellow-800' :
            'bg-blue-100 text-blue-800'
          }`}>
            {contract.status}
          </span>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-semibold mb-4">Contract Details</h2>
          <dl className="space-y-3">
            <div>
              <dt className="text-sm font-medium text-gray-500">Contract Number</dt>
              <dd className="mt-1 text-sm text-gray-900">{contract.contractNumber}</dd>
            </div>
            <div>
              <dt className="text-sm font-medium text-gray-500">Title</dt>
              <dd className="mt-1 text-sm text-gray-900">{contract.title}</dd>
            </div>
            {contract.description && (
              <div>
                <dt className="text-sm font-medium text-gray-500">Description</dt>
                <dd className="mt-1 text-sm text-gray-900">{contract.description}</dd>
              </div>
            )}
            {contract.contractValue && (
              <div>
                <dt className="text-sm font-medium text-gray-500">Contract Value</dt>
                <dd className="mt-1 text-sm text-gray-900 font-semibold">
                  {contract.currency ? `${contract.currency} ${contract.contractValue.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}` : contract.contractValue.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                </dd>
              </div>
            )}
            <div>
              <dt className="text-sm font-medium text-gray-500">Start Date</dt>
              <dd className="mt-1 text-sm text-gray-900">{new Date(contract.startDate).toLocaleDateString()}</dd>
            </div>
            <div>
              <dt className="text-sm font-medium text-gray-500">End Date</dt>
              <dd className="mt-1 text-sm text-gray-900">{new Date(contract.endDate).toLocaleDateString()}</dd>
            </div>
            {contract.contractType && (
              <div>
                <dt className="text-sm font-medium text-gray-500">Contract Type</dt>
                <dd className="mt-1 text-sm text-gray-900">{contract.contractType}</dd>
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
                <Link href={`/vendors/${contract.vendorId}`} className="text-blue-600 hover:text-blue-800">
                  {contract.vendorName}
                </Link>
              </dd>
            </div>
          </dl>
        </div>

        {contract.termsAndConditions && (
          <div className="bg-white rounded-lg shadow p-6 md:col-span-2">
            <h2 className="text-xl font-semibold mb-4">Terms and Conditions</h2>
            <p className="text-sm text-gray-900 whitespace-pre-wrap">{contract.termsAndConditions}</p>
          </div>
        )}

        {contract.renewalTerms && (
          <div className="bg-white rounded-lg shadow p-6">
            <h2 className="text-xl font-semibold mb-4">Renewal Terms</h2>
            <p className="text-sm text-gray-900 whitespace-pre-wrap">{contract.renewalTerms}</p>
          </div>
        )}

        {contract.terminationClause && (
          <div className="bg-white rounded-lg shadow p-6">
            <h2 className="text-xl font-semibold mb-4">Termination Clause</h2>
            <p className="text-sm text-gray-900 whitespace-pre-wrap">{contract.terminationClause}</p>
          </div>
        )}
      </div>
    </div>
  )
}

