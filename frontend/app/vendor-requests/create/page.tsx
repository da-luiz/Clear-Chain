'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { createVendorRequest, getDepartments, getVendorCategories, uploadFile, Department, VendorCategory } from '@/lib/api'
import { getCurrentUser, canCreateVendorRequest, User } from '@/lib/permissions'
import { getErrorMessage } from '@/lib/errorHandler'
import Link from 'next/link'

export default function CreateVendorRequestPage() {
  const router = useRouter()
  const [user, setUser] = useState<User | null>(null)
  const [departments, setDepartments] = useState<Department[]>([])
  const [categories, setCategories] = useState<VendorCategory[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [formData, setFormData] = useState({
    companyName: '',
    legalName: '',
    expectedContractValue: '',
    currency: '',
    requestingDepartmentId: '',
    // Contact details
    primaryContactName: '',
    primaryContactTitle: '',
    primaryContactEmail: '',
    primaryContactPhone: '',
    // Additional company info
    businessRegistrationNumber: '',
    businessType: '',
    website: '',
    categoryId: '',
    // Address fields
    addressStreet: '',
    addressCity: '',
    addressState: '',
    addressPostalCode: '',
    addressCountry: '',
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
  
  // Supporting documents state
  const [supportingDocs, setSupportingDocs] = useState<Array<{type: string, value: string, name: string, fileName?: string}>>([])
  const [newDocType, setNewDocType] = useState<'file' | 'link' | 'github' | 'linkedin'>('link')
  const [newDocValue, setNewDocValue] = useState<string>('')
  const [newDocName, setNewDocName] = useState<string>('')
  const [uploadingFile, setUploadingFile] = useState(false)

  useEffect(() => {
    const currentUser = getCurrentUser()
    setUser(currentUser)
    
    // Check permissions
    if (!canCreateVendorRequest(currentUser)) {
      router.push('/')
      return
    }
    
    loadData()
  }, [router])

  const loadData = async () => {
    try {
      const [deptsData, categoriesData] = await Promise.all([
        getDepartments(),
        getVendorCategories()
      ])
      setDepartments(deptsData.filter(d => d.isActive))
      setCategories(categoriesData.filter(c => c.isActive))
    } catch (err) {
      console.error('Error loading data:', err)
      setError('Failed to load data. Make sure the backend is running.')
    }
  }

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement>) => {
    const { name, value } = e.target
    setFormData(prev => ({
      ...prev,
      [name]: value
    }))
  }
  
  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return
    
    setUploadingFile(true)
    setError(null)
    
    try {
      const uploadResult = await uploadFile(file)
      const newDoc = {
        type: 'file' as const,
        value: uploadResult.url,
        name: newDocName.trim() || file.name,
        fileName: uploadResult.fileName
      }
      setSupportingDocs([...supportingDocs, newDoc])
      setNewDocName('')
      e.target.value = '' // Reset file input
    } catch (err: any) {
      console.error('Error uploading file:', err)
      setError(getErrorMessage(err) || 'Failed to upload file')
    } finally {
      setUploadingFile(false)
    }
  }
  
  const handleAddSupportingDoc = () => {
    if (newDocType === 'file') {
      // File uploads are handled by handleFileUpload
      return
    }
    
    if (!newDocValue.trim()) {
      setError('Please enter a value for the supporting document')
      return
    }
    
    const newDoc = {
      type: newDocType,
      value: newDocValue.trim(),
      name: newDocName.trim() || newDocValue.trim()
    }
    setSupportingDocs([...supportingDocs, newDoc])
    setNewDocValue('')
    setNewDocName('')
  }
  
  const handleRemoveSupportingDoc = (index: number) => {
    setSupportingDocs(supportingDocs.filter((_, i) => i !== index))
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setError(null)

    if (!user) {
      setError('User not authenticated. Please log in.')
      setLoading(false)
      return
    }

    try {
      const requestData = {
        companyName: formData.companyName,
        legalName: formData.legalName || undefined,
        expectedContractValue: formData.expectedContractValue ? parseFloat(formData.expectedContractValue) : undefined,
        currency: formData.currency || undefined,
        requestingDepartmentId: parseInt(formData.requestingDepartmentId),
        requestedByUserId: user.userId,
        // Contact details
        primaryContactName: formData.primaryContactName || undefined,
        primaryContactTitle: formData.primaryContactTitle || undefined,
        primaryContactEmail: formData.primaryContactEmail || undefined,
        primaryContactPhone: formData.primaryContactPhone || undefined,
        // Additional company info
        businessRegistrationNumber: formData.businessRegistrationNumber || undefined,
        businessType: formData.businessType || undefined,
        website: formData.website || undefined,
        categoryId: formData.categoryId ? parseInt(formData.categoryId) : undefined,
        // Address fields
        addressStreet: formData.addressStreet || undefined,
        addressCity: formData.addressCity || undefined,
        addressState: formData.addressState || undefined,
        addressPostalCode: formData.addressPostalCode || undefined,
        addressCountry: formData.addressCountry || undefined,
        // Supporting documents
        supportingDocuments: supportingDocs.length > 0 ? JSON.stringify(supportingDocs) : undefined,
      }

      const created = await createVendorRequest(requestData)
      router.push(`/vendor-requests/${created.id}`)
    } catch (err: any) {
      console.error('Error creating vendor request:', err)
      setError(getErrorMessage(err) || 'Failed to create vendor request')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="max-w-4xl mx-auto p-6">
      <div className="mb-6">
        <Link href="/vendor-requests" className="text-blue-600 hover:text-blue-800 mb-4 inline-block">
          ← Back to Vendor Requests
        </Link>
        <h1 className="text-3xl font-bold">Create Vendor Request</h1>
        <p className="text-gray-600 mt-2">Fill in the company and contact information below</p>
      </div>

      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Basic Company Info */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-semibold mb-4">Basic Company Information</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Company Name *
              </label>
              <input
                type="text"
                name="companyName"
                value={formData.companyName}
                onChange={handleChange}
                required
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Legal Name
              </label>
              <input
                type="text"
                name="legalName"
                value={formData.legalName}
                onChange={handleChange}
                placeholder="Legal business name"
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Business Registration Number
              </label>
              <input
                type="text"
                name="businessRegistrationNumber"
                value={formData.businessRegistrationNumber}
                onChange={handleChange}
                placeholder="Registration number"
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Business Type
              </label>
              <input
                type="text"
                name="businessType"
                value={formData.businessType}
                onChange={handleChange}
                placeholder="e.g., LLC, Corporation"
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Website
              </label>
              <input
                type="url"
                name="website"
                value={formData.website}
                onChange={handleChange}
                placeholder="https://example.com"
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            {categories.length > 0 && (
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Category
                </label>
                <select
                  name="categoryId"
                  value={formData.categoryId}
                  onChange={handleChange}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="">Select a category</option>
                  {categories.map(cat => (
                    <option key={cat.id} value={cat.id}>{cat.name}</option>
                  ))}
                </select>
              </div>
            )}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Expected Contract Value
              </label>
              <input
                type="number"
                name="expectedContractValue"
                value={formData.expectedContractValue}
                onChange={handleChange}
                step="0.01"
                min="0"
                placeholder="0.00"
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            {formData.expectedContractValue && (
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Currency *
                </label>
                <select
                  name="currency"
                  value={formData.currency}
                  onChange={handleChange}
                  required={!!formData.expectedContractValue}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
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
          </div>
        </div>

        {/* Contact Details */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-semibold mb-4">Contact Details</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Primary Contact Name
              </label>
              <input
                type="text"
                name="primaryContactName"
                value={formData.primaryContactName}
                onChange={handleChange}
                placeholder="Contact person name"
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Title / Role
              </label>
              <input
                type="text"
                name="primaryContactTitle"
                value={formData.primaryContactTitle}
                onChange={handleChange}
                placeholder="e.g., Account Manager"
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Email
              </label>
              <input
                type="email"
                name="primaryContactEmail"
                value={formData.primaryContactEmail}
                onChange={handleChange}
                placeholder="contact@example.com"
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Phone Number
              </label>
              <input
                type="tel"
                name="primaryContactPhone"
                value={formData.primaryContactPhone}
                onChange={handleChange}
                placeholder="+1 234 567 8900"
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
          </div>
        </div>

        {/* Address */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-semibold mb-4">Address</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="md:col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Street Address
              </label>
              <input
                type="text"
                name="addressStreet"
                value={formData.addressStreet}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                City
              </label>
              <input
                type="text"
                name="addressCity"
                value={formData.addressCity}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                State/Province
              </label>
              <input
                type="text"
                name="addressState"
                value={formData.addressState}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Postal Code
              </label>
              <input
                type="text"
                name="addressPostalCode"
                value={formData.addressPostalCode}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Country
              </label>
              <input
                type="text"
                name="addressCountry"
                value={formData.addressCountry}
                onChange={handleChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
          </div>
        </div>

        {/* Supporting Documents */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-semibold mb-4">Supporting Documents</h2>
          <p className="text-sm text-gray-600 mb-4">
            Upload documents (banking details, certificates, etc.) or add links (website, GitHub, LinkedIn) for Compliance and Finance approvers to review
          </p>
          
          {/* List existing documents */}
          {supportingDocs.length > 0 && (
            <div className="mb-4 space-y-2">
              {supportingDocs.map((doc, index) => (
                <div key={index} className="flex items-center justify-between p-3 bg-gray-50 rounded border">
                  <div className="flex-1">
                    <div className="flex items-center gap-2 mb-1">
                      <span className="px-2 py-1 text-xs font-medium bg-blue-100 text-blue-800 rounded">
                        {doc.type.toUpperCase()}
                      </span>
                      <span className="font-medium">{doc.name}</span>
                      {doc.fileName && doc.fileName !== doc.name && (
                        <span className="text-xs text-gray-500">({doc.fileName})</span>
                      )}
                    </div>
                    <a 
                      href={doc.value.startsWith('http') ? doc.value : `http://localhost:8080${doc.value}`}
                      target="_blank" 
                      rel="noopener noreferrer"
                      className="text-sm text-blue-600 hover:underline break-all"
                    >
                      {doc.value}
                    </a>
                  </div>
                  <button
                    type="button"
                    onClick={() => handleRemoveSupportingDoc(index)}
                    className="ml-4 text-red-600 hover:text-red-800"
                  >
                    Remove
                  </button>
                </div>
              ))}
            </div>
          )}
          
          {/* Add new document */}
          <div className="border-t pt-4">
            <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Type
                </label>
                <select
                  value={newDocType}
                  onChange={(e) => {
                    const newType = e.target.value as 'file' | 'link' | 'github' | 'linkedin'
                    setNewDocType(newType)
                    // Clear values when switching types to prevent controlled/uncontrolled issues
                    if (newType === 'file') {
                      setNewDocValue('')
                    }
                  }}
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="file">Upload File</option>
                  <option value="link">Website Link</option>
                  <option value="github">GitHub</option>
                  <option value="linkedin">LinkedIn</option>
                </select>
              </div>
              {newDocType === 'file' ? (
                <>
                  <div className="md:col-span-2">
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Upload File
                    </label>
                    <input
                      type="file"
                      onChange={handleFileUpload}
                      disabled={uploadingFile}
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-gray-100"
                    />
                    {uploadingFile && <p className="text-xs text-gray-500 mt-1">Uploading...</p>}
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Display Name (Optional)
                    </label>
                    <input
                      type="text"
                      value={newDocName || ''}
                      onChange={(e) => setNewDocName(e.target.value)}
                      placeholder="Document name"
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                  </div>
                </>
              ) : (
                <>
                  <div className="md:col-span-2">
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      URL
                    </label>
                    <input
                      key={`url-${newDocType}`}
                      type="text"
                      value={newDocValue}
                      onChange={(e) => setNewDocValue(e.target.value)}
                      placeholder={
                        newDocType === 'github' ? 'https://github.com/user/repo' :
                        newDocType === 'linkedin' ? 'https://linkedin.com/company/name' :
                        'https://example.com'
                      }
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Display Name (Optional)
                    </label>
                    <input
                      type="text"
                      value={newDocName || ''}
                      onChange={(e) => setNewDocName(e.target.value)}
                      placeholder="Link name"
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                  </div>
                </>
              )}
            </div>
            {newDocType !== 'file' && (
              <button
                type="button"
                onClick={handleAddSupportingDoc}
                className="px-4 py-2 bg-gray-600 text-white rounded-md hover:bg-gray-700"
              >
                Add {newDocType === 'github' ? 'GitHub' : newDocType === 'linkedin' ? 'LinkedIn' : 'Link'}
              </button>
            )}
          </div>
        </div>

        {/* Request Details */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-semibold mb-4">Request Details</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Requesting Department <span className="text-red-500">*</span>
              </label>
              {departments.length > 0 ? (
                <select
                  name="requestingDepartmentId"
                  value={formData.requestingDepartmentId}
                  onChange={handleChange}
                  required
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="">Select a department *</option>
                  {departments.map(dept => (
                    <option key={dept.id} value={dept.id}>{dept.name}</option>
                  ))}
                </select>
              ) : (
                <div className="w-full px-3 py-2 border border-red-300 rounded-md bg-red-50 text-red-700">
                  No departments available. Please create a department first.
                </div>
              )}
            </div>
          </div>
        </div>

        <div className="flex justify-end space-x-4">
          <Link
            href="/vendor-requests"
            className="px-6 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50"
          >
            Cancel
          </Link>
          <button
            type="submit"
            disabled={loading}
            className="px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:bg-gray-400"
          >
            {loading ? 'Creating...' : 'Create Request'}
          </button>
        </div>
      </form>
    </div>
  )
}

