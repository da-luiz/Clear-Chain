import axios from 'axios';

// Base URL for the Spring Boot backend
// Use environment variable in production, fallback to localhost for development
// In production, this will be https://app.clearchain.space/api (set via NEXT_PUBLIC_API_URL)
// In development, use http://localhost:8080/api
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 
  (typeof window !== 'undefined' && window.location.origin === 'https://app.clearchain.space' 
    ? 'https://app.clearchain.space/api' 
    : 'http://localhost:8080/api');

// Create axios instance with default config
// Note: In browser environment, we can't set httpsAgent
// The browser will show a security warning for self-signed certificates
// User needs to accept the certificate in the browser
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// NO AUTHENTICATION - Just store token for frontend state management
api.interceptors.request.use((config) => {
  // Token is stored but not validated by backend
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Handle errors (but no authentication redirects)
api.interceptors.response.use(
  (response) => response,
  (error) => {
    // Just log errors, no redirects
    return Promise.reject(error)
  }
)

// Types (interfaces for TypeScript)
export interface Department {
  id: number;
  name: string;
  code: string;
  description?: string;
  isActive: boolean;
}

export interface User {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
  departmentId?: number;
  departmentName?: string;
  active: boolean;
}

export interface VendorCategory {
  id: number;
  name: string;
  code: string;
  description?: string;
  isActive: boolean;
}

export interface Vendor {
  id: number;
  vendorCode: string;
  companyName: string;
  email?: string;
  phone?: string;
  status: string;
  categoryId?: number;
  categoryName?: string;
  street?: string;
  city?: string;
  state?: string;
  postalCode?: string;
  country?: string;
  website?: string;
  description?: string;
}

export interface PurchaseOrder {
  id: number;
  poNumber: string;
  vendorId: number;
  vendorName: string;
  description: string;
  totalAmount: number;
  currency?: string;
  status: string;
  orderDate: string;
  expectedDeliveryDate?: string;
  deliveryAddress?: string;
  paymentTerms?: string;
  notes?: string;
  createdByUserId?: number;
  createdByUsername?: string;
  approvedByUserId?: number;
  approvedByUsername?: string;
  approvedAt?: string;
  rejectionReason?: string;
}

export interface Contract {
  id: number;
  vendorId: number;
  vendorName: string;
  contractNumber: string;
  title: string;
  description?: string;
  contractValue?: number;
  currency?: string;
  startDate: string;
  endDate: string;
  contractType?: string;
  status: string;
  termsAndConditions?: string;
  createdByUserId?: number;
  createdByUsername?: string;
  approvedByUserId?: number;
  approvedByUsername?: string;
  approvedAt?: string;
  renewalTerms?: string;
  terminationClause?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface DashboardSummary {
  totalActiveUsers: number;
  totalVendors: number;
  activeVendors: number;
  pendingVendors: number;
  inactiveVendors: number;
  suspendedVendors: number;
  pendingVendorRequests: number;
  submittedRequests: number;
  underReviewRequests: number;
  totalPurchaseOrders: number;
  totalContracts: number;
  totalPurchaseOrderSpend?: number;
  totalContractValue?: number;
  totalSpendYtd?: number;
  pendingApprovalsCount?: number;
  latestVendors: Vendor[];
  latestVendorRequests: any[];
  latestUsers: User[];
  pendingApprovalPurchaseOrders: PurchaseOrder[];
  expiringContracts: Contract[];
}

export interface VendorCreationRequest {
  id: number;
  requestNumber: string;
  status: string;
  vendorId?: number;
  companyName: string;
  legalName?: string;
  businessJustification?: string;
  expectedContractValue?: number;
  requestingDepartmentId: number;
  requestingDepartmentName?: string;
  requestedByUserId: number;
  requestedByUsername?: string;
  rejectionReason?: string;
  additionalInfoRequired?: string;
  reviewedByUserId?: number;
  reviewedByUsername?: string;
  reviewedAt?: string;
  createdAt: string;
  updatedAt?: string;
  // Contact details
  primaryContactName?: string;
  primaryContactTitle?: string;
  primaryContactEmail?: string;
  primaryContactPhone?: string;
  // Additional company info
  businessRegistrationNumber?: string;
  taxIdentificationNumber?: string;
  businessType?: string;
  website?: string;
  categoryId?: number;
  categoryName?: string;
  // Address fields
  addressStreet?: string;
  addressCity?: string;
  addressState?: string;
  addressPostalCode?: string;
  addressCountry?: string;
  // Banking and payment details (filled by Finance)
  bankName?: string;
  accountHolderName?: string;
  accountNumber?: string;
  swiftBicCode?: string;
  currency?: string; // Selected by Requester, visible to Finance
  paymentTerms?: string;
  preferredPaymentMethod?: string;
  // Supporting documents: uploaded files, links, GitHub, LinkedIn
  supportingDocuments?: string; // JSON array: [{"type":"file|link|github|linkedin","value":"...","name":"...","fileName":"..."}]
}

// API Functions

// Authentication
export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  username: string;
  role: string;
  userId: number;
  firstName: string;
  lastName: string;
}

export const login = async (username: string, password: string): Promise<LoginResponse> => {
  const response = await api.post<LoginResponse>('/auth/login', { username, password });
  return response.data;
};

// Dashboard
export const getDashboard = async (): Promise<DashboardSummary> => {
  const response = await api.get<DashboardSummary>('/dashboard');
  return response.data;
};

// Departments
export const getDepartments = async (): Promise<Department[]> => {
  const response = await api.get<Department[]>('/departments');
  return response.data;
};

export const createDepartment = async (department: {
  name: string;
  code: string;
  description?: string;
}): Promise<Department> => {
  const response = await api.post<Department>('/departments', department);
  return response.data;
};

// Bootstrap
export const getBootstrapStatus = async (): Promise<{ initialized: boolean }> => {
  const response = await api.get<{ initialized: boolean }>('/bootstrap/status');
  return response.data;
};

export const createFirstAdmin = async (admin: {
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  password: string;
}): Promise<User> => {
  const response = await api.post<User>('/bootstrap/create-admin', admin);
  return response.data;
};

// Users
export const getUsers = async (): Promise<User[]> => {
  const response = await api.get<User[]>('/users');
  return response.data;
};

export const createUser = async (user: {
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  role: string;
  departmentId?: number;
  password?: string;
}): Promise<User> => {
  const response = await api.post<User>('/users', user);
  return response.data;
};

// Vendor Categories
export const getVendorCategories = async (): Promise<VendorCategory[]> => {
  const response = await api.get<VendorCategory[]>('/vendor-categories');
  return response.data;
};

export const createVendorCategory = async (category: {
  name: string;
  code: string;
  description?: string;
}): Promise<VendorCategory> => {
  const response = await api.post<VendorCategory>('/vendor-categories', category);
  return response.data;
};

// Vendors
export const getVendors = async (limit: number = 10): Promise<Vendor[]> => {
  const response = await api.get<Vendor[]>(`/vendors?limit=${limit}`);
  return response.data;
};

export const getVendor = async (id: number): Promise<Vendor> => {
  const response = await api.get<Vendor>(`/vendors/${id}`);
  return response.data;
};

// Direct vendor creation removed - vendors must be created through VendorCreationRequest workflow
// export const createVendor = async (vendor: { ... }): Promise<Vendor> => { ... }

export const updateVendor = async (id: number, vendor: {
  companyName: string;
  email?: string;
  phone?: string;
  categoryId?: number;
  street?: string;
  city?: string;
  state?: string;
  country?: string;
  website?: string;
  description?: string;
}): Promise<Vendor> => {
  const response = await api.put<Vendor>(`/vendors/${id}`, vendor);
  return response.data;
};

export const activateVendor = async (id: number): Promise<void> => {
  await api.post(`/vendors/${id}/activate`);
};

export const suspendVendor = async (id: number): Promise<void> => {
  await api.post(`/vendors/${id}/suspend`);
};

export const terminateVendor = async (id: number): Promise<void> => {
  await api.post(`/vendors/${id}/terminate`);
};

// Purchase Orders
export const getPurchaseOrdersForVendor = async (vendorId: number): Promise<PurchaseOrder[]> => {
  const response = await api.get<PurchaseOrder[]>(`/purchase-orders/vendor/${vendorId}`);
  return response.data;
};

export const getPurchaseOrder = async (id: number): Promise<PurchaseOrder> => {
  const response = await api.get<PurchaseOrder>(`/purchase-orders/${id}`);
  return response.data;
};

export const createPurchaseOrder = async (po: {
  vendorId: number;
  description: string;
  totalAmount: number;
  currency?: string;
  orderDate: string;
  expectedDeliveryDate?: string;
  createdByUserId: number;
  deliveryAddress?: string;
  paymentTerms?: string;
  notes?: string;
}): Promise<PurchaseOrder> => {
  const response = await api.post<PurchaseOrder>('/purchase-orders', po);
  return response.data;
};

export const submitPurchaseOrderForApproval = async (id: number): Promise<PurchaseOrder> => {
  const response = await api.post<PurchaseOrder>(`/purchase-orders/${id}/submit`);
  return response.data;
};

export const approvePurchaseOrder = async (id: number, approverId: number): Promise<PurchaseOrder> => {
  const response = await api.post<PurchaseOrder>(`/purchase-orders/${id}/approve?approverId=${approverId}`);
  return response.data;
};

export const rejectPurchaseOrder = async (id: number, approverId: number, rejectionReason: string): Promise<PurchaseOrder> => {
  const response = await api.post<PurchaseOrder>(`/purchase-orders/${id}/reject?approverId=${approverId}&rejectionReason=${encodeURIComponent(rejectionReason)}`);
  return response.data;
};

export const sendPurchaseOrder = async (id: number): Promise<PurchaseOrder> => {
  const response = await api.post<PurchaseOrder>(`/purchase-orders/${id}/send`);
  return response.data;
};

export const markPurchaseOrderAsReceived = async (id: number): Promise<PurchaseOrder> => {
  const response = await api.post<PurchaseOrder>(`/purchase-orders/${id}/receive`);
  return response.data;
};

// Contracts
export const getContract = async (id: number): Promise<Contract> => {
  const response = await api.get<Contract>(`/contracts/${id}`);
  return response.data;
};

export const getContractsForVendor = async (vendorId: number): Promise<Contract[]> => {
  const response = await api.get<Contract[]>(`/contracts/vendor/${vendorId}`);
  return response.data;
};

export const createContract = async (contract: {
  vendorId: number;
  contractNumber: string;
  title: string;
  description?: string;
  contractValue?: number;
  currency?: string;
  startDate: string;
  endDate: string;
  contractType?: string;
  termsAndConditions?: string;
  createdByUserId: number;
  renewalTerms?: string;
  terminationClause?: string;
}): Promise<Contract> => {
  const response = await api.post<Contract>('/contracts', contract);
  return response.data;
};

export const approveContract = async (id: number, approverId: number): Promise<Contract> => {
  const response = await api.post<Contract>(`/contracts/${id}/approve?approverId=${approverId}`);
  return response.data;
};

// Vendor Creation Requests
export const getVendorRequests = async (): Promise<VendorCreationRequest[]> => {
  const response = await api.get<VendorCreationRequest[]>('/vendor-requests/pending');
  return response.data;
};

export const getVendorRequest = async (id: number): Promise<VendorCreationRequest> => {
  const response = await api.get<VendorCreationRequest>(`/vendor-requests/${id}`);
  return response.data;
};

export const createVendorRequest = async (request: {
  companyName: string;
  legalName?: string;
  businessJustification?: string;
  expectedContractValue?: number;
  requestingDepartmentId: number;
  requestedByUserId: number;
  // Contact details
  primaryContactName?: string;
  primaryContactTitle?: string;
  primaryContactEmail?: string;
  primaryContactPhone?: string;
  // Additional company info
  businessRegistrationNumber?: string;
  taxIdentificationNumber?: string;
  businessType?: string;
  website?: string;
  categoryId?: number;
  // Address fields
  addressStreet?: string;
  addressCity?: string;
  addressState?: string;
  addressPostalCode?: string;
  addressCountry?: string;
  // Currency and supporting documents
  currency?: string; // Selected by Requester
  supportingDocuments?: string; // JSON array: [{"type":"file|link|github|linkedin","value":"...","name":"...","fileName":"..."}]
}): Promise<VendorCreationRequest> => {
  const response = await api.post<VendorCreationRequest>('/vendor-requests', request);
  return response.data;
};

export const updateVendorRequest = async (id: number, request: {
  companyName: string;
  legalName?: string;
  businessJustification?: string;
  expectedContractValue?: number;
  requestingDepartmentId: number;
  // Contact details
  primaryContactName?: string;
  primaryContactTitle?: string;
  primaryContactEmail?: string;
  primaryContactPhone?: string;
  // Additional company info
  businessRegistrationNumber?: string;
  taxIdentificationNumber?: string;
  businessType?: string;
  website?: string;
  categoryId?: number;
  // Address fields
  addressStreet?: string;
  addressCity?: string;
  addressState?: string;
  addressPostalCode?: string;
  addressCountry?: string;
}): Promise<VendorCreationRequest> => {
  const response = await api.put<VendorCreationRequest>(`/vendor-requests/${id}`, request);
  return response.data;
};

export const submitVendorRequest = async (id: number): Promise<VendorCreationRequest> => {
  const response = await api.post<VendorCreationRequest>(`/vendor-requests/${id}/submit`);
  return response.data;
};

export const addBankingDetails = async (id: number, details: {
  bankName: string;
  accountHolderName: string;
  accountNumber: string;
  swiftBicCode?: string;
  currency?: string;
  paymentTerms?: string;
  preferredPaymentMethod?: string;
}): Promise<VendorCreationRequest> => {
  const response = await api.post<VendorCreationRequest>(`/vendor-requests/${id}/banking-details`, details);
  return response.data;
};

// Finance approval/rejection
export const approveVendorRequestByFinance = async (id: number, reviewerId: number, comment?: string): Promise<VendorCreationRequest> => {
  const response = await api.post<VendorCreationRequest>(`/vendor-requests/${id}/finance/approve`, { reviewerId, comment });
  return response.data;
};

export const rejectVendorRequestByFinance = async (id: number, reviewerId: number, comment: string): Promise<VendorCreationRequest> => {
  const response = await api.post<VendorCreationRequest>(`/vendor-requests/${id}/finance/reject`, { reviewerId, comment });
  return response.data;
};

// Compliance approval/rejection
export const approveVendorRequestByCompliance = async (id: number, reviewerId: number, comment?: string): Promise<VendorCreationRequest> => {
  const response = await api.post<VendorCreationRequest>(`/vendor-requests/${id}/compliance/approve`, { reviewerId, comment });
  return response.data;
};

export const rejectVendorRequestByCompliance = async (id: number, reviewerId: number, comment: string): Promise<VendorCreationRequest> => {
  const response = await api.post<VendorCreationRequest>(`/vendor-requests/${id}/compliance/reject`, { reviewerId, comment });
  return response.data;
};

// Admin approval/rejection
export const approveVendorRequestByAdmin = async (id: number, reviewerId: number, comment?: string): Promise<VendorCreationRequest> => {
  const response = await api.post<VendorCreationRequest>(`/vendor-requests/${id}/admin/approve`, { reviewerId, comment });
  return response.data;
};

export const rejectVendorRequestByAdmin = async (id: number, reviewerId: number, comment: string): Promise<VendorCreationRequest> => {
  const response = await api.post<VendorCreationRequest>(`/vendor-requests/${id}/admin/reject`, { reviewerId, comment });
  return response.data;
};

// Legacy methods for backward compatibility
export const approveVendorRequest = async (id: number, reviewerId: number, comment?: string): Promise<VendorCreationRequest> => {
  const response = await api.post<VendorCreationRequest>(`/vendor-requests/${id}/approve`, { reviewerId, comment });
  return response.data;
};

export const rejectVendorRequest = async (id: number, reviewerId: number, comment: string): Promise<VendorCreationRequest> => {
  const response = await api.post<VendorCreationRequest>(`/vendor-requests/${id}/reject`, { reviewerId, comment });
  return response.data;
};

// File Upload
export const uploadFile = async (file: File): Promise<{fileName: string, filePath: string, url: string, size: string, contentType: string}> => {
  const formData = new FormData();
  formData.append('file', file);
  
  const response = await api.post<{fileName: string, filePath: string, url: string, size: string, contentType: string}>('/files/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
  return response.data;
};

export default api;

