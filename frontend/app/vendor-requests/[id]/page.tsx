'use client'

import { useEffect, useState } from 'react'
import { useRouter, useParams } from 'next/navigation'
import { getVendorRequest, submitVendorRequest, approveVendorRequestByFinance, rejectVendorRequestByFinance, approveVendorRequestByCompliance, rejectVendorRequestByCompliance, approveVendorRequestByAdmin, rejectVendorRequestByAdmin, addBankingDetails, VendorCreationRequest } from '@/lib/api'
import { getCurrentUser, canSubmitVendorRequest, canAddBankingDetails, canApproveVendorRequestByFinance, canApproveVendorRequestByCompliance, canApproveVendorRequestByAdmin, User } from '@/lib/permissions'
import { getErrorMessage } from '@/lib/errorHandler'
import Link from 'next/link'

export default function VendorRequestDetailPage() {
  const router = useRouter()
  const params = useParams()
  const id = parseInt(params.id as string)
  
  const [user, setUser] = useState<User | null>(null)
  const [request, setRequest] = useState<VendorCreationRequest | null>(null)
  const [loading, setLoading] = useState(true)
  const [actionLoading, setActionLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [showBankingForm, setShowBankingForm] = useState(false)
  const [showApproveForm, setShowApproveForm] = useState(false)
  const [showRejectForm, setShowRejectForm] = useState(false)
  
  const [bankingData, setBankingData] = useState({
    bankName: '',
    accountHolderName: '',
    accountNumber: '',
    swiftBicCode: '',
    currency: '',
    paymentTerms: '',
    preferredPaymentMethod: '',
  })
  
  const [actionData, setActionData] = useState({
    comment: '',
  })

  useEffect(() => {
    loadData()
  }, [id])

  useEffect(() => {
    const currentUser = getCurrentUser()
    setUser(currentUser)
  }, [])

  const loadData = async () => {
    try {
      setLoading(true)
      const requestData = await getVendorRequest(id)
      setRequest(requestData)
      
      // Pre-fill banking data if exists
      if (requestData.bankName) {
        setBankingData({
          bankName: requestData.bankName || '',
          accountHolderName: requestData.accountHolderName || '',
          accountNumber: requestData.accountNumber || '',
          swiftBicCode: requestData.swiftBicCode || '',
          currency: requestData.currency || '',
          paymentTerms: requestData.paymentTerms || '',
          preferredPaymentMethod: requestData.preferredPaymentMethod || '',
        })
      }
    } catch (err: any) {
      setError(getErrorMessage(err) || 'Failed to load vendor request')
      console.error('Error loading vendor request:', err)
    } finally {
      setLoading(false)
    }
  }

  const handleSubmit = async () => {
    setActionLoading(true)
    setError(null)
    try {
      await submitVendorRequest(id)
      loadData()
    } catch (err: any) {
      setError(getErrorMessage(err) || 'Failed to submit request')
    } finally {
      setActionLoading(false)
    }
  }

  const handleAddBankingDetails = async (e: React.FormEvent) => {
    e.preventDefault()
    setActionLoading(true)
    setError(null)
    try {
      await addBankingDetails(id, bankingData)
      setShowBankingForm(false)
      loadData()
    } catch (err: any) {
      setError(getErrorMessage(err) || 'Failed to add banking details')
    } finally {
      setActionLoading(false)
    }
  }

  const handleApprove = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!user || !request) {
      setError('User not found. Please login again.')
      return
    }
    
    setActionLoading(true)
    setError(null)
    try {
      // Determine which approval method based on current status (new workflow: Compliance → Finance → Admin)
      // No comments needed for approval - just approve
      if (request.status === 'PENDING_COMPLIANCE_REVIEW') {
        await approveVendorRequestByCompliance(id, user.userId, '')
      } else if (request.status === 'PENDING_FINANCE_REVIEW') {
        await approveVendorRequestByFinance(id, user.userId, '')
      } else if (request.status === 'PENDING_ADMIN_REVIEW') {
        await approveVendorRequestByAdmin(id, user.userId, '')
      } else {
        throw new Error('Request is not in a state that can be approved')
      }
      setShowApproveForm(false)
      loadData()
      router.push('/vendor-requests')
    } catch (err: any) {
      setError(getErrorMessage(err) || 'Failed to approve request')
    } finally {
      setActionLoading(false)
    }
  }

  const handleReject = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!user || !request) {
      setError('User not found. Please login again.')
      return
    }
    
    if (!actionData.comment || actionData.comment.trim() === '') {
      setError('Rejection reason is required')
      return
    }
    
    setActionLoading(true)
    setError(null)
    try {
      // Determine which rejection method based on current status (new workflow: Compliance → Finance → Admin)
      if (request.status === 'PENDING_COMPLIANCE_REVIEW') {
        await rejectVendorRequestByCompliance(id, user.userId, actionData.comment)
      } else if (request.status === 'PENDING_FINANCE_REVIEW') {
        await rejectVendorRequestByFinance(id, user.userId, actionData.comment)
      } else if (request.status === 'PENDING_ADMIN_REVIEW') {
        await rejectVendorRequestByAdmin(id, user.userId, actionData.comment)
      } else {
        throw new Error('Request is not in a state that can be rejected')
      }
      setShowRejectForm(false)
      loadData()
    } catch (err: any) {
      setError(getErrorMessage(err) || 'Failed to reject request')
    } finally {
      setActionLoading(false)
    }
  }

  if (loading) {
    return <div className="flex justify-center items-center h-64">Loading...</div>
  }

  if (!request) {
    return <div className="text-red-600">Vendor request not found</div>
  }

  // Permission-based access control for new workflow: Compliance → Finance → Admin
  const canSubmit = request.status === 'DRAFT' && canSubmitVendorRequest(user)
  const canAddBanking = request.status === 'PENDING_FINANCE_REVIEW' && canAddBankingDetails(user)
  const canApproveCompliance = request.status === 'PENDING_COMPLIANCE_REVIEW' && canApproveVendorRequestByCompliance(user)
  const canRejectCompliance = request.status === 'PENDING_COMPLIANCE_REVIEW' && canApproveVendorRequestByCompliance(user)
  // Finance can only approve if banking details are present
  const hasBankingDetails = request.bankName && request.accountNumber && request.accountHolderName
  const canApproveFinance = request.status === 'PENDING_FINANCE_REVIEW' && canApproveVendorRequestByFinance(user) && hasBankingDetails
  const canRejectFinance = request.status === 'PENDING_FINANCE_REVIEW' && canApproveVendorRequestByFinance(user)
  const canApproveAdmin = request.status === 'PENDING_ADMIN_REVIEW' && canApproveVendorRequestByAdmin(user)
  const canRejectAdmin = request.status === 'PENDING_ADMIN_REVIEW' && canApproveVendorRequestByAdmin(user)
  
  // Parse supporting documents
  const supportingDocs = request.supportingDocuments ? (() => {
    try {
      return JSON.parse(request.supportingDocuments)
    } catch {
      return []
    }
  })() : []

  return (
    <div className="max-w-6xl mx-auto p-6">
      <div className="mb-6">
        <Link href="/vendor-requests" className="text-blue-600 hover:text-blue-800 mb-4 inline-block">
          ← Back to Vendor Requests
        </Link>
        <div className="flex justify-between items-center">
          <div>
            <h1 className="text-3xl font-bold">{request.companyName}</h1>
            <p className="text-gray-600 mt-1">Request #{request.requestNumber}</p>
          </div>
          <span className={`px-4 py-2 rounded-full text-sm font-semibold ${
            request.status === 'ACTIVE' ? 'bg-green-100 text-green-800' :
            request.status === 'REJECTED_BY_FINANCE' || request.status === 'REJECTED_BY_COMPLIANCE' || request.status === 'REJECTED_BY_ADMIN' ? 'bg-red-100 text-red-800' :
            request.status === 'PENDING_ADMIN_REVIEW' ? 'bg-purple-100 text-purple-800' :
            request.status === 'PENDING_FINANCE_REVIEW' ? 'bg-blue-100 text-blue-800' :
            request.status === 'PENDING_COMPLIANCE_REVIEW' ? 'bg-yellow-100 text-yellow-800' :
            request.status === 'DRAFT' ? 'bg-gray-100 text-gray-800' :
            'bg-gray-100 text-gray-800'
          }`}>
            {request.status.replace(/_/g, ' ')}
          </span>
        </div>
      </div>

      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
          {error}
        </div>
      )}

      {/* Actions */}
      <div className="bg-white rounded-lg shadow p-6 mb-6">
        <h2 className="text-xl font-semibold mb-4">Actions</h2>
        <div className="flex flex-wrap gap-3">
          {canSubmit && (
            <button
              onClick={handleSubmit}
              disabled={actionLoading}
              className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700 disabled:bg-gray-400"
            >
              Submit Request
            </button>
          )}
          {canAddBanking && (
            <button
              onClick={() => setShowBankingForm(!showBankingForm)}
              className="px-4 py-2 bg-purple-600 text-white rounded hover:bg-purple-700"
            >
              {request.bankName ? 'Update Banking Details' : 'Add Banking Details'}
            </button>
          )}
          {(canApproveCompliance || canApproveFinance || canApproveAdmin) && (
            <button
              onClick={() => {
                setShowApproveForm(!showApproveForm)
                if (!showApproveForm) {
                  setTimeout(() => {
                    document.getElementById('approve-form')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
                  }, 100)
                }
              }}
              disabled={request.status === 'PENDING_FINANCE_REVIEW' && !hasBankingDetails}
              className="px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700 disabled:bg-gray-400 disabled:cursor-not-allowed"
              title={request.status === 'PENDING_FINANCE_REVIEW' && !hasBankingDetails ? 'Banking details required before approval' : ''}
            >
              Approve
            </button>
          )}
          {(canRejectCompliance || canRejectFinance || canRejectAdmin) && (
            <button
              onClick={() => {
                setShowRejectForm(!showRejectForm)
                if (!showRejectForm) {
                  setTimeout(() => {
                    document.getElementById('reject-form')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
                  }, 100)
                }
              }}
              className="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700"
            >
              Reject
            </button>
          )}
          {request.status === 'PENDING_FINANCE_REVIEW' && !hasBankingDetails && canApproveVendorRequestByFinance(user) && (
            <span className="text-sm text-red-600 font-medium">
              ⚠️ Banking details required before approval
            </span>
          )}
        </div>
      </div>

      {/* Banking Details Form */}
      {showBankingForm && (
        <div className="bg-white rounded-lg shadow p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">Banking & Payment Details</h2>
          <form onSubmit={handleAddBankingDetails} className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Bank Name *</label>
                <input
                  type="text"
                  required
                  value={bankingData.bankName}
                  onChange={(e) => setBankingData({...bankingData, bankName: e.target.value})}
                  className="w-full px-3 py-2 border rounded-md"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Account Holder Name *</label>
                <input
                  type="text"
                  required
                  value={bankingData.accountHolderName}
                  onChange={(e) => setBankingData({...bankingData, accountHolderName: e.target.value})}
                  className="w-full px-3 py-2 border rounded-md"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Account Number / IBAN *</label>
                <input
                  type="text"
                  required
                  value={bankingData.accountNumber}
                  onChange={(e) => setBankingData({...bankingData, accountNumber: e.target.value})}
                  className="w-full px-3 py-2 border rounded-md"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">SWIFT / BIC Code</label>
                <input
                  type="text"
                  value={bankingData.swiftBicCode}
                  onChange={(e) => setBankingData({...bankingData, swiftBicCode: e.target.value})}
                  className="w-full px-3 py-2 border rounded-md"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Currency *</label>
                <select
                  required
                  value={bankingData.currency}
                  onChange={(e) => setBankingData({...bankingData, currency: e.target.value})}
                  className="w-full px-3 py-2 border rounded-md"
                >
                  <option value="">Select currency</option>
                  <option value="NGN">NGN - Nigerian Naira</option>
                  <option value="USD">USD - US Dollar</option>
                  <option value="EUR">EUR - Euro</option>
                  <option value="GBP">GBP - British Pound</option>
                  <option value="JPY">JPY - Japanese Yen</option>
                  <option value="AUD">AUD - Australian Dollar</option>
                  <option value="CAD">CAD - Canadian Dollar</option>
                  <option value="CHF">CHF - Swiss Franc</option>
                  <option value="CNY">CNY - Chinese Yuan</option>
                  <option value="INR">INR - Indian Rupee</option>
                  <option value="SGD">SGD - Singapore Dollar</option>
                  <option value="HKD">HKD - Hong Kong Dollar</option>
                  <option value="ZAR">ZAR - South African Rand</option>
                  <option value="BRL">BRL - Brazilian Real</option>
                  <option value="MXN">MXN - Mexican Peso</option>
                  <option value="KRW">KRW - South Korean Won</option>
                  <option value="TRY">TRY - Turkish Lira</option>
                  <option value="RUB">RUB - Russian Ruble</option>
                  <option value="AED">AED - UAE Dirham</option>
                  <option value="SAR">SAR - Saudi Riyal</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Payment Terms</label>
                <input
                  type="text"
                  value={bankingData.paymentTerms}
                  onChange={(e) => setBankingData({...bankingData, paymentTerms: e.target.value})}
                  placeholder="Net 30, Net 60, Upon Delivery"
                  className="w-full px-3 py-2 border rounded-md"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Preferred Payment Method</label>
                <input
                  type="text"
                  value={bankingData.preferredPaymentMethod}
                  onChange={(e) => setBankingData({...bankingData, preferredPaymentMethod: e.target.value})}
                  className="w-full px-3 py-2 border rounded-md"
                />
              </div>
            </div>
            <div className="flex justify-end space-x-3">
              <button
                type="button"
                onClick={() => setShowBankingForm(false)}
                className="px-4 py-2 border rounded-md"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={actionLoading}
                className="px-4 py-2 bg-purple-600 text-white rounded hover:bg-purple-700 disabled:bg-gray-400"
              >
                Save Banking Details
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Workflow Visualization */}
      <div className="bg-white rounded-lg shadow p-6 mb-6">
        <h2 className="text-xl font-semibold mb-4">Approval Workflow</h2>
        <div className="flex items-center justify-between">
          <div className={`flex-1 text-center p-4 rounded ${request.status === 'PENDING_COMPLIANCE_REVIEW' || request.status === 'REJECTED_BY_COMPLIANCE' ? 'bg-yellow-100 border-2 border-yellow-400' : request.status === 'ACTIVE' || request.status === 'PENDING_FINANCE_REVIEW' || request.status === 'PENDING_ADMIN_REVIEW' ? 'bg-green-100' : 'bg-gray-100'}`}>
            <div className="font-semibold">1. Compliance Review</div>
            <div className="text-sm text-gray-600 mt-1">
              {request.status === 'PENDING_COMPLIANCE_REVIEW' ? '⏳ In Progress' :
               request.status === 'REJECTED_BY_COMPLIANCE' ? '❌ Rejected' :
               request.status === 'ACTIVE' || request.status === 'PENDING_FINANCE_REVIEW' || request.status === 'PENDING_ADMIN_REVIEW' ? '✅ Approved' :
               '⏸️ Pending'}
            </div>
          </div>
          <div className="mx-4 text-gray-400">→</div>
          <div className={`flex-1 text-center p-4 rounded ${request.status === 'PENDING_FINANCE_REVIEW' || request.status === 'REJECTED_BY_FINANCE' ? 'bg-blue-100 border-2 border-blue-400' : request.status === 'ACTIVE' || request.status === 'PENDING_ADMIN_REVIEW' ? 'bg-green-100' : 'bg-gray-100'}`}>
            <div className="font-semibold">2. Finance Review</div>
            <div className="text-sm text-gray-600 mt-1">
              {request.status === 'PENDING_FINANCE_REVIEW' ? '⏳ In Progress' :
               request.status === 'REJECTED_BY_FINANCE' ? '❌ Rejected' :
               request.status === 'ACTIVE' || request.status === 'PENDING_ADMIN_REVIEW' ? '✅ Approved' :
               '⏸️ Pending'}
            </div>
            {request.status === 'PENDING_FINANCE_REVIEW' && request.currency && (
              <div className="text-xs text-blue-700 mt-2 font-medium">
                Currency: {request.currency}
              </div>
            )}
          </div>
          <div className="mx-4 text-gray-400">→</div>
          <div className={`flex-1 text-center p-4 rounded ${request.status === 'PENDING_ADMIN_REVIEW' || request.status === 'REJECTED_BY_ADMIN' ? 'bg-purple-100 border-2 border-purple-400' : request.status === 'ACTIVE' ? 'bg-green-100' : 'bg-gray-100'}`}>
            <div className="font-semibold">3. Admin Review</div>
            <div className="text-sm text-gray-600 mt-1">
              {request.status === 'PENDING_ADMIN_REVIEW' ? '⏳ In Progress' :
               request.status === 'REJECTED_BY_ADMIN' ? '❌ Rejected' :
               request.status === 'ACTIVE' ? '✅ Approved - Vendor Created' :
               '⏸️ Pending'}
            </div>
          </div>
        </div>
      </div>

      {/* Supporting Documents */}
      {supportingDocs.length > 0 && (
        <div className="bg-white rounded-lg shadow p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">Supporting Documents</h2>
          <p className="text-sm text-gray-600 mb-4">
            Documents and links provided by requester for Compliance and Finance review
          </p>
          <div className="space-y-3">
            {supportingDocs.map((doc: any, index: number) => {
              const docUrl = doc.value?.startsWith('http') 
                ? doc.value 
                : `http://localhost:8080${doc.value}`;
              return (
                <div key={index} className="flex items-center justify-between p-3 bg-gray-50 rounded border">
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-1">
                      <span className="px-2 py-1 text-xs font-medium bg-blue-100 text-blue-800 rounded">
                        {doc.type?.toUpperCase() || 'LINK'}
                      </span>
                      <span className="font-medium">{doc.name || doc.fileName || 'Document'}</span>
                      {doc.fileName && doc.fileName !== doc.name && (
                        <span className="text-xs text-gray-500">({doc.fileName})</span>
                      )}
                    </div>
                    <a 
                      href={docUrl}
                      target="_blank" 
                      rel="noopener noreferrer"
                      className="text-sm text-blue-600 hover:underline break-all"
                    >
                      {doc.value}
                    </a>
                    {doc.type === 'file' && (
                      <p className="text-xs text-gray-500 mt-1">📄 Uploaded file - Click to view/download</p>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* Approve Form */}
      {showApproveForm && (
        <div id="approve-form" className="bg-white rounded-lg shadow p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">Approve Request</h2>
          <form onSubmit={handleApprove} className="space-y-4">
            <div className="bg-green-50 border-l-4 border-green-400 p-4 rounded">
              <p className="text-green-800 font-medium">Ready to approve this request?</p>
              <p className="text-sm text-green-700 mt-1">Click "Approve" to proceed. No comments required.</p>
            </div>
            <div className="flex justify-end space-x-3">
              <button
                type="button"
                onClick={() => setShowApproveForm(false)}
                className="px-4 py-2 border rounded-md"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={actionLoading}
                className="px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700 disabled:bg-gray-400"
              >
                Approve
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Reject Form */}
      {showRejectForm && (
        <div id="reject-form" className="bg-white rounded-lg shadow p-6 mb-6">
          <h2 className="text-xl font-semibold mb-4">Reject Request</h2>
          <form onSubmit={handleReject} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Rejection Reason *
              </label>
              <textarea
                value={actionData.comment}
                onChange={(e) => setActionData({...actionData, comment: e.target.value})}
                rows={3}
                placeholder="Please provide a reason for rejection"
                required
                className="w-full px-3 py-2 border rounded-md"
              />
            </div>
            <div className="flex justify-end space-x-3">
              <button
                type="button"
                onClick={() => setShowRejectForm(false)}
                className="px-4 py-2 border rounded-md"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={actionLoading}
                className="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700 disabled:bg-gray-400"
              >
                Reject
              </button>
            </div>
          </form>
        </div>
      )}

      {/* Request Details */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Company Info */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-semibold mb-4">Company Information</h2>
          <dl className="space-y-3">
            <div>
              <dt className="text-sm font-medium text-gray-500">Company Name</dt>
              <dd className="mt-1 text-sm text-gray-900">{request.companyName}</dd>
            </div>
            {request.legalName && (
              <div>
                <dt className="text-sm font-medium text-gray-500">Legal Name</dt>
                <dd className="mt-1 text-sm text-gray-900">{request.legalName}</dd>
              </div>
            )}
            {request.categoryName && (
              <div>
                <dt className="text-sm font-medium text-gray-500">Category</dt>
                <dd className="mt-1 text-sm text-gray-900">{request.categoryName}</dd>
              </div>
            )}
            {request.businessType && (
              <div>
                <dt className="text-sm font-medium text-gray-500">Business Type</dt>
                <dd className="mt-1 text-sm text-gray-900">{request.businessType}</dd>
              </div>
            )}
            {request.website && (
              <div>
                <dt className="text-sm font-medium text-gray-500">Website</dt>
                <dd className="mt-1 text-sm text-gray-900">
                  <a href={request.website} target="_blank" rel="noopener noreferrer" className="text-blue-600 hover:underline">
                    {request.website}
                  </a>
                </dd>
              </div>
            )}
            {request.businessJustification && (
              <div>
                <dt className="text-sm font-medium text-gray-500">Business Justification</dt>
                <dd className="mt-1 text-sm text-gray-900">{request.businessJustification}</dd>
              </div>
            )}
            {request.expectedContractValue && (
              <div>
                <dt className="text-sm font-medium text-gray-500">Expected Contract Value</dt>
                <dd className="mt-1 text-sm text-gray-900">${request.expectedContractValue.toLocaleString()}</dd>
              </div>
            )}
          </dl>
        </div>

        {/* Contact Details */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-semibold mb-4">Contact Details</h2>
          <dl className="space-y-3">
            {request.primaryContactName && (
              <div>
                <dt className="text-sm font-medium text-gray-500">Contact Name</dt>
                <dd className="mt-1 text-sm text-gray-900">{request.primaryContactName}</dd>
              </div>
            )}
            {request.primaryContactTitle && (
              <div>
                <dt className="text-sm font-medium text-gray-500">Title/Role</dt>
                <dd className="mt-1 text-sm text-gray-900">{request.primaryContactTitle}</dd>
              </div>
            )}
            {request.primaryContactEmail && (
              <div>
                <dt className="text-sm font-medium text-gray-500">Email</dt>
                <dd className="mt-1 text-sm text-gray-900">{request.primaryContactEmail}</dd>
              </div>
            )}
            {request.primaryContactPhone && (
              <div>
                <dt className="text-sm font-medium text-gray-500">Phone</dt>
                <dd className="mt-1 text-sm text-gray-900">{request.primaryContactPhone}</dd>
              </div>
            )}
          </dl>
        </div>

        {/* Address */}
        {(request.addressStreet || request.addressCity || request.addressCountry) && (
          <div className="bg-white rounded-lg shadow p-6">
            <h2 className="text-xl font-semibold mb-4">Address</h2>
            <dl className="space-y-3">
              {request.addressStreet && (
                <div>
                  <dt className="text-sm font-medium text-gray-500">Street</dt>
                  <dd className="mt-1 text-sm text-gray-900">{request.addressStreet}</dd>
                </div>
              )}
              <div className="grid grid-cols-2 gap-3">
                {request.addressCity && (
                  <div>
                    <dt className="text-sm font-medium text-gray-500">City</dt>
                    <dd className="mt-1 text-sm text-gray-900">{request.addressCity}</dd>
                  </div>
                )}
                {request.addressState && (
                  <div>
                    <dt className="text-sm font-medium text-gray-500">State</dt>
                    <dd className="mt-1 text-sm text-gray-900">{request.addressState}</dd>
                  </div>
                )}
                {request.addressPostalCode && (
                  <div>
                    <dt className="text-sm font-medium text-gray-500">Postal Code</dt>
                    <dd className="mt-1 text-sm text-gray-900">{request.addressPostalCode}</dd>
                  </div>
                )}
                {request.addressCountry && (
                  <div>
                    <dt className="text-sm font-medium text-gray-500">Country</dt>
                    <dd className="mt-1 text-sm text-gray-900">{request.addressCountry}</dd>
                  </div>
                )}
              </div>
            </dl>
          </div>
        )}

        {/* Banking Details */}
        {request.bankName && (
          <div className="bg-white rounded-lg shadow p-6">
            <h2 className="text-xl font-semibold mb-4">Banking & Payment Details</h2>
            <dl className="space-y-3">
              {request.currency && (
                <div className="bg-blue-50 p-3 rounded border-l-4 border-blue-400">
                  <dt className="text-sm font-medium text-blue-700">Currency (Selected by Requester)</dt>
                  <dd className="mt-1 text-lg font-semibold text-blue-900">{request.currency}</dd>
                </div>
              )}
              <div>
                <dt className="text-sm font-medium text-gray-500">Bank Name</dt>
                <dd className="mt-1 text-sm text-gray-900">{request.bankName}</dd>
              </div>
              <div>
                <dt className="text-sm font-medium text-gray-500">Account Holder</dt>
                <dd className="mt-1 text-sm text-gray-900">{request.accountHolderName}</dd>
              </div>
              <div>
                <dt className="text-sm font-medium text-gray-500">Account Number</dt>
                <dd className="mt-1 text-sm text-gray-900">{request.accountNumber}</dd>
              </div>
              {request.swiftBicCode && (
                <div>
                  <dt className="text-sm font-medium text-gray-500">SWIFT/BIC</dt>
                  <dd className="mt-1 text-sm text-gray-900">{request.swiftBicCode}</dd>
                </div>
              )}
              {request.currency && (
                <div>
                  <dt className="text-sm font-medium text-gray-500">Currency</dt>
                  <dd className="mt-1 text-sm text-gray-900">{request.currency}</dd>
                </div>
              )}
              {request.paymentTerms && (
                <div>
                  <dt className="text-sm font-medium text-gray-500">Payment Terms</dt>
                  <dd className="mt-1 text-sm text-gray-900">{request.paymentTerms}</dd>
                </div>
              )}
              {request.preferredPaymentMethod && (
                <div>
                  <dt className="text-sm font-medium text-gray-500">Payment Method</dt>
                  <dd className="mt-1 text-sm text-gray-900">{request.preferredPaymentMethod}</dd>
                </div>
              )}
            </dl>
          </div>
        )}
      </div>
    </div>
  )
}

