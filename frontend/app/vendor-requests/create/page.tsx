'use client'

import { useState, useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { createVendorRequest, submitVendorRequest, getDepartments, getVendorCategories, uploadFile, createDepartment, Department, VendorCategory } from '@/lib/api'
import { getCurrentUser, canCreateVendorRequest, canManageUsers, User } from '@/lib/permissions'
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
    serviceDescription: '',
    reasonForAdding: '',
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
  // Supporting documents state
  const [supportingDocs, setSupportingDocs] = useState<Array<{type: string, value: string, name: string, fileName?: string}>>([])
  const [newDocType, setNewDocType] = useState<'file' | 'link' | 'github' | 'linkedin'>('link')
  const [newDocValue, setNewDocValue] = useState<string>('')
  const [newDocName, setNewDocName] = useState<string>('')
  const [uploadingFile, setUploadingFile] = useState(false)
  const [selectedFiles, setSelectedFiles] = useState<File[]>([])

  // Department creation (admin-only)
  const [showCreateDepartment, setShowCreateDepartment] = useState(false)
  const [departmentForm, setDepartmentForm] = useState({ name: '', code: '', description: '' })
  const [creatingDepartment, setCreatingDepartment] = useState(false)

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
      // Do not hide departments only because they are inactive.
      // If there are any departments in the system, the requester should be able to pick one.
      setDepartments(deptsData)
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
  
  const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files ? Array.from(e.target.files) : []
    setSelectedFiles(files)
    // Allow re-selecting the same file(s) again
    e.target.value = ''
    setError(null)
  }

  const handleUploadSelectedFiles = async () => {
    if (selectedFiles.length === 0) {
      setError('Please select at least one file to upload')
      return
    }

    setUploadingFile(true)
    setError(null)

    try {
      const createdDocs: Array<{type: string, value: string, name: string, fileName?: string}> = []
      const displayName = newDocName.trim()

      for (const file of selectedFiles) {
        const uploadResult = await uploadFile(file)
        createdDocs.push({
          type: 'file',
          value: uploadResult.url,
          name: displayName || file.name,
          fileName: uploadResult.fileName,
        })
      }

      setSupportingDocs(prev => [...prev, ...createdDocs])
      setNewDocName('')
      setSelectedFiles([])
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
    
    const raw = newDocValue || ''
    const values = raw
      .split(/[\r\n,]+/g)
      .map(v => v.trim())
      .filter(Boolean)

    if (values.length === 0) {
      setError('Please enter a value for the supporting document')
      return
    }

    const displayName = newDocName.trim()
    const createdDocs = values.map((value) => ({
      type: newDocType,
      value,
      name: displayName || value,
    }))

    setSupportingDocs(prev => [...prev, ...createdDocs])
    setNewDocValue('')
    setNewDocName('')
  }
  
  const handleRemoveSupportingDoc = (index: number) => {
    setSupportingDocs(supportingDocs.filter((_, i) => i !== index))
  }

  const handleCreateDepartment = async () => {
    if (!user || !canManageUsers(user)) return

    const name = (departmentForm.name || '').trim()
    const code = (departmentForm.code || '').trim()
    const description = (departmentForm.description || '').trim()

    if (!name) {
      setError('Department name is required')
      return
    }
    if (!code) {
      setError('Department code is required')
      return
    }

    setCreatingDepartment(true)
    setError(null)

    try {
      await createDepartment({ name, code, description: description || undefined })
      setShowCreateDepartment(false)
      setDepartmentForm({ name: '', code: '', description: '' })
      await loadData()
    } catch (err: any) {
      console.error('Error creating department:', err)
      setError(getErrorMessage(err) || 'Failed to create department')
    } finally {
      setCreatingDepartment(false)
    }
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
      if (!formData.companyName.trim()) throw new Error('Vendor name is required')
      if (!formData.primaryContactEmail.trim()) throw new Error('Vendor email is required')
      if (!formData.primaryContactPhone.trim()) throw new Error('Vendor phone is required')
      if (!formData.addressStreet.trim()) throw new Error('Vendor address is required')
      if (!formData.categoryId) throw new Error('Service category is required')
      if (!formData.serviceDescription.trim()) throw new Error('Service description is required')
      if (!formData.reasonForAdding.trim()) throw new Error('Reason for adding this vendor is required')
      if (!formData.requestingDepartmentId) throw new Error('Requesting department is required')

      const businessJustification = [
        `Service Description: ${formData.serviceDescription.trim()}`,
        `Reason for adding this vendor: ${formData.reasonForAdding.trim()}`
      ].join('\n')

      const requestData = {
        companyName: formData.companyName,
        legalName: formData.legalName || undefined,
        businessJustification,
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
      await submitVendorRequest(created.id)
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
          ← Back to Vendors
        </Link>
        <h1 className="text-3xl font-bold">Create Vendor</h1>
        <p className="text-gray-600 mt-2">Complete the form and submit for compliance review.</p>
      </div>

      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
          {error}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Section A — Basic Information */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-semibold mb-4">Section A — Basic Information</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Vendor Name *
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
                Service Category *
                </label>
                <select
                  name="categoryId"
                  value={formData.categoryId}
                  onChange={handleChange}
                required
                  className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="">Select a category</option>
                  {categories.map(cat => (
                    <option key={cat.id} value={cat.id}>{cat.name}</option>
                  ))}
                </select>
              </div>
            )}
            <div className="md:col-span-2">
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Service Description *
              </label>
              <textarea
                name="serviceDescription"
                value={formData.serviceDescription}
                onChange={handleChange}
                required
                rows={3}
                placeholder="Describe the services this vendor will provide"
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
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
                Vendor Email *
              </label>
              <input
                type="email"
                name="primaryContactEmail"
                value={formData.primaryContactEmail}
                onChange={handleChange}
                required
                placeholder="contact@example.com"
                className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Vendor Phone *
              </label>
              <input
                type="tel"
                name="primaryContactPhone"
                value={formData.primaryContactPhone}
                onChange={handleChange}
                required
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
                Vendor Address *
              </label>
              <input
                type="text"
                name="addressStreet"
                value={formData.addressStreet}
                onChange={handleChange}
                required
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

        {/* Section B — Business Justification */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-semibold mb-4">Section B — Business Justification</h2>
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Reason for adding this vendor *
          </label>
          <textarea
            name="reasonForAdding"
            value={formData.reasonForAdding}
            onChange={handleChange}
            required
            rows={4}
            placeholder="Explain why this vendor should be added."
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
        </div>

        {/* Section C — Document Upload */}
        <div className="bg-white rounded-lg shadow p-6">
          <h2 className="text-xl font-semibold mb-4">Section C — Document Upload</h2>
          <p className="text-sm text-gray-600 mb-4">
            Upload supporting documents like company profile, proposal, portfolio, or certifications.
            Financial documents are not required at requester stage.
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
                      multiple
                      onChange={handleFileUpload}
                      disabled={uploadingFile}
                      className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-gray-100"
                    />
                    {selectedFiles.length > 0 && (
                      <p className="text-xs text-gray-600 mt-1">
                        Selected {selectedFiles.length} file{selectedFiles.length === 1 ? '' : 's'}
                      </p>
                    )}
                    {uploadingFile && <p className="text-xs text-gray-500 mt-1">Uploading...</p>}

                    <div className="mt-3">
                      <button
                        type="button"
                        onClick={handleUploadSelectedFiles}
                        disabled={uploadingFile || selectedFiles.length === 0}
                        className="px-4 py-2 bg-gray-600 text-white rounded-md hover:bg-gray-700 disabled:bg-gray-300"
                      >
                        Upload Files
                      </button>
                    </div>
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
                      URL(s)
                    </label>
                    <textarea
                      key={`url-${newDocType}`}
                      rows={3}
                      value={newDocValue}
                      onChange={(e) => setNewDocValue(e.target.value)}
                      placeholder={
                        newDocType === 'github'
                          ? 'https://github.com/user/repo\n(you can paste multiple, one per line)'
                          : newDocType === 'linkedin'
                            ? 'https://linkedin.com/company/name\n(you can paste multiple, one per line)'
                            : 'https://example.com\n(you can paste multiple, one per line)'
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
                    <option key={dept.id} value={dept.id}>
                      {dept.name}{dept.isActive ? '' : ' (inactive)'}
                    </option>
                  ))}
                </select>
              ) : (
                <div className="w-full px-3 py-2 border border-red-300 rounded-md bg-red-50 text-red-700">
                  No departments found. Ask your Admin to create at least one department, then refresh this page.
                </div>
              )}

              {user && canManageUsers(user) && (
                <div className="mt-3">
                  <button
                    type="button"
                    onClick={() => {
                      setShowCreateDepartment(true)
                      setDepartmentForm({ name: '', code: '', description: '' })
                      setError(null)
                    }}
                    className="px-4 py-2 bg-emerald-600 text-white rounded-md hover:bg-emerald-700"
                  >
                    Add Department
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>

        {showCreateDepartment && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
            <div className="w-full max-w-lg bg-white rounded-lg shadow-lg p-6">
              <h3 className="text-xl font-semibold mb-2">Create Department</h3>
              <p className="text-sm text-gray-600 mb-4">This will populate the Requesting Department dropdown.</p>

              <div className="space-y-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Department Name *</label>
                  <input
                    type="text"
                    value={departmentForm.name}
                    onChange={(e) => setDepartmentForm(prev => ({ ...prev, name: e.target.value }))}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-emerald-500"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Department Code *</label>
                  <input
                    type="text"
                    value={departmentForm.code}
                    onChange={(e) => setDepartmentForm(prev => ({ ...prev, code: e.target.value }))}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-emerald-500"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
                  <textarea
                    rows={3}
                    value={departmentForm.description}
                    onChange={(e) => setDepartmentForm(prev => ({ ...prev, description: e.target.value }))}
                    className="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-emerald-500"
                  />
                </div>

                {error && (
                  <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
                    {error}
                  </div>
                )}
              </div>

              <div className="flex justify-end gap-3 mt-6">
                <button
                  type="button"
                  onClick={() => setShowCreateDepartment(false)}
                  className="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50"
                  disabled={creatingDepartment}
                >
                  Cancel
                </button>
                <button
                  type="button"
                  onClick={handleCreateDepartment}
                  disabled={creatingDepartment}
                  className="px-4 py-2 bg-emerald-600 text-white rounded-md hover:bg-emerald-700 disabled:bg-emerald-300"
                >
                  {creatingDepartment ? 'Creating...' : 'Create'}
                </button>
              </div>
            </div>
          </div>
        )}

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
            {loading ? 'Submitting...' : 'Submit Vendor Request'}
          </button>
        </div>
      </form>
    </div>
  )
}

