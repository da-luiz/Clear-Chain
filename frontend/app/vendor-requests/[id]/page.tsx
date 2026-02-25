'use client'

import { useEffect, useState } from 'react'
import { useRouter, useParams } from 'next/navigation'
import { getVendorRequest, submitVendorRequest, approveVendorRequestByCompliance, rejectVendorRequestByCompliance, approveVendorRequestByAdmin, rejectVendorRequestByAdmin, VendorCreationRequest } from '@/lib/api'
import { getCurrentUser, canSubmitVendorRequest, canApproveVendorRequestByCompliance, canApproveVendorRequestByAdmin, User } from '@/lib/permissions'
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
  const [showApproveForm, setShowApproveForm] = useState(false)
  const [showRejectForm, setShowRejectForm] = useState(false)
  
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

  const handleApprove = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!user || !request) {
      setError('User not found. Please login again.')
      return
    }
    
    setActionLoading(true)
    setError(null)
    try {
      // Workflow: Compliance -> Admin
      // No comments needed for approval - just approve
      if (request.status === 'PENDING_COMPLIANCE_REVIEW') {
        await approveVendorRequestByCompliance(id, user.userId, '')
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
      // Workflow: Compliance -> Admin
      if (request.status === 'PENDING_COMPLIANCE_REVIEW') {
        await rejectVendorRequestByCompliance(id, user.userId, actionData.comment)
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

  // Permission-based access control for workflow: Compliance -> Admin
  const canSubmit = request.status === 'DRAFT' && canSubmitVendorRequest(user)
  const canApproveCompliance = request.status === 'PENDING_COMPLIANCE_REVIEW' && canApproveVendorRequestByCompliance(user)
  const canRejectCompliance = request.status === 'PENDING_COMPLIANCE_REVIEW' && canApproveVendorRequestByCompliance(user)
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

  const statusLabel = (() => {
    switch (request.status) {
      case 'PENDING_COMPLIANCE_REVIEW':
        return 'Pending Compliance Review'
      case 'PENDING_ADMIN_REVIEW':
        return 'Pending Admin Approval'
      case 'ACTIVE':
        return 'Approved'
      case 'REJECTED_BY_COMPLIANCE':
      case 'REJECTED_BY_FINANCE':
      case 'REJECTED_BY_ADMIN':
        return 'Rejected'
      default:
        return request.status.replace(/_/g, ' ')
    }
  })()

  const statusColor = (() => {
    if (request.status === 'ACTIVE') return 'bg-green-100 text-green-800'
    if (request.status.startsWith('REJECTED')) return 'bg-red-100 text-red-800'
    if (request.status === 'PENDING_COMPLIANCE_REVIEW') return 'bg-yellow-100 text-yellow-800'
    if (request.status === 'PENDING_ADMIN_REVIEW') return 'bg-purple-100 text-purple-800'
    if (request.status === 'DRAFT') return 'bg-gray-100 text-gray-800'
    return 'bg-gray-100 text-gray-800'
  })()

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
          <span className={`px-4 py-2 rounded-full text-sm font-semibold ${statusColor}`}>
            {statusLabel}
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
          {(canApproveCompliance || canApproveAdmin) && (
            <button
              onClick={() => {
                setShowApproveForm(!showApproveForm)
                if (!showApproveForm) {
                  setTimeout(() => {
                    document.getElementById('approve-form')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
                  }, 100)
                }
              }}
              className="px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700 disabled:bg-gray-400 disabled:cursor-not-allowed"
            >
              Approve
            </button>
          )}
          {(canRejectCompliance || canRejectAdmin) && (
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
        </div>
      </div>

      {/* Workflow Visualization */}
      <div className="bg-white rounded-lg shadow p-6 mb-6">
        <h2 className="text-xl font-semibold mb-4">Status Timeline</h2>
        <div className="mb-4 text-sm text-gray-600">
          Track the vendor onboarding journey from submission to final decision.
        </div>
        {request.rejectionReason && (
          <div className="mb-4 bg-red-50 border border-red-200 rounded p-3">
            <p className="text-sm font-semibold text-red-700">Rejection Reason</p>
            <p className="text-sm text-red-700 mt-1">{request.rejectionReason}</p>
          </div>
        )}
        {request.additionalInfoRequired && (
          <div className="mb-4 bg-amber-50 border border-amber-200 rounded p-3">
            <p className="text-sm font-semibold text-amber-700">Reviewer Comment</p>
            <p className="text-sm text-amber-700 mt-1">{request.additionalInfoRequired}</p>
          </div>
        )}
        <div className="flex items-center justify-between">
          <div className={`flex-1 text-center p-4 rounded ${request.status === 'PENDING_COMPLIANCE_REVIEW' || request.status === 'REJECTED_BY_COMPLIANCE' ? 'bg-yellow-100 border-2 border-yellow-400' : request.status === 'ACTIVE' || request.status === 'PENDING_ADMIN_REVIEW' ? 'bg-green-100' : 'bg-gray-100'}`}>
            <div className="font-semibold">1. Compliance Review</div>
            <div className="text-sm text-gray-600 mt-1">
              {request.status === 'PENDING_COMPLIANCE_REVIEW' ? '⏳ In Progress' :
               request.status === 'REJECTED_BY_COMPLIANCE' ? '❌ Rejected' :
               request.status === 'ACTIVE' || request.status === 'PENDING_ADMIN_REVIEW' ? '✅ Approved' :
               '⏸️ Pending'}
            </div>
          </div>
          <div className="mx-4 text-gray-400">→</div>
          <div className={`flex-1 text-center p-4 rounded ${request.status === 'PENDING_ADMIN_REVIEW' || request.status === 'REJECTED_BY_ADMIN' ? 'bg-purple-100 border-2 border-purple-400' : request.status === 'ACTIVE' ? 'bg-green-100' : 'bg-gray-100'}`}>
            <div className="font-semibold">2. Admin Decision</div>
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
            Documents and links provided by requester for Compliance and Admin review
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

      </div>
    </div>
  )
}

