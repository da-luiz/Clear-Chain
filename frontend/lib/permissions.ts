/**
 * Role-based permission utilities
 * Based on the backend SecurityConfig permissions
 */

export type UserRole = 
  | 'ADMIN'
  | 'DEPARTMENT_REQUESTER'
  | 'FINANCE_APPROVER'
  | 'COMPLIANCE_APPROVER';

export interface User {
  username: string;
  role: UserRole | string;
  userId: number;
  firstName: string;
  lastName: string;
}

/**
 * Get current user from localStorage
 */
export const getCurrentUser = (): User | null => {
  if (typeof window === 'undefined') return null;
  
  const userStr = localStorage.getItem('user');
  if (!userStr) return null;
  
  try {
    return JSON.parse(userStr);
  } catch {
    return null;
  }
};

/**
 * Check if user has a specific role
 */
export const hasRole = (user: User | null, roles: UserRole[]): boolean => {
  if (!user) return false;
  return roles.includes(user.role as UserRole);
};

/**
 * Check if user can create vendor requests
 */
export const canCreateVendorRequest = (user: User | null): boolean => {
  return hasRole(user, [
    'ADMIN',
    'DEPARTMENT_REQUESTER'
  ]);
};

/**
 * Check if user can edit vendor request drafts
 */
export const canEditVendorRequestDraft = (user: User | null): boolean => {
  return hasRole(user, ['ADMIN', 'DEPARTMENT_REQUESTER']);
};

/**
 * Check if user can submit vendor requests
 */
export const canSubmitVendorRequest = (user: User | null): boolean => {
  return hasRole(user, ['ADMIN', 'DEPARTMENT_REQUESTER']);
};

/**
 * Check if user can add banking details to vendor requests (Finance can add during review)
 */
export const canAddBankingDetails = (user: User | null): boolean => {
  return hasRole(user, [
    'ADMIN',
    'FINANCE_APPROVER'
  ]);
};

/**
 * Check if user can approve/reject vendor requests in Finance stage
 */
export const canApproveVendorRequestByFinance = (user: User | null): boolean => {
  return hasRole(user, [
    'ADMIN',
    'FINANCE_APPROVER'
  ]);
};

/**
 * Check if user can approve/reject vendor requests in Compliance stage
 */
export const canApproveVendorRequestByCompliance = (user: User | null): boolean => {
  return hasRole(user, [
    'ADMIN',
    'COMPLIANCE_APPROVER'
  ]);
};

/**
 * Check if user can approve/reject vendor requests by Admin (final approval)
 */
export const canApproveVendorRequestByAdmin = (user: User | null): boolean => {
  return hasRole(user, ['ADMIN']);
};

/**
 * Check if user can approve/reject vendor requests (legacy - checks Finance, Compliance, and Admin)
 */
export const canApproveVendorRequest = (user: User | null): boolean => {
  return canApproveVendorRequestByFinance(user) || canApproveVendorRequestByCompliance(user) || canApproveVendorRequestByAdmin(user);
};

/**
 * Check if user can view vendors
 */
export const canViewVendors = (user: User | null): boolean => {
  return user !== null; // All authenticated users can view vendors
};

/**
 * Check if user can edit vendors (Admin and Compliance can edit)
 */
export const canEditVendors = (user: User | null): boolean => {
  return hasRole(user, [
    'ADMIN',
    'COMPLIANCE_APPROVER'
  ]);
};

/**
 * Check if user is admin (ADMIN_SYSTEM_OWNER role)
 */
export const isAdmin = (user: User | null): boolean => {
  // Backend uses ADMIN_SYSTEM_OWNER but enum might be ADMIN
  return hasRole(user, ['ADMIN']) || (user?.role === 'ADMIN_SYSTEM_OWNER');
};

/**
 * Check if user can create purchase orders
 */
export const canCreatePurchaseOrder = (user: User | null): boolean => {
  return hasRole(user, ['ADMIN', 'DEPARTMENT_REQUESTER', 'FINANCE_APPROVER']);
};

/**
 * Check if user can approve purchase orders (Finance and Admin)
 */
export const canApprovePurchaseOrder = (user: User | null): boolean => {
  return hasRole(user, ['ADMIN', 'FINANCE_APPROVER']);
};

/**
 * Check if user can create contracts (Admin and Compliance)
 */
export const canCreateContract = (user: User | null): boolean => {
  return hasRole(user, ['ADMIN', 'COMPLIANCE_APPROVER']);
};

/**
 * Check if user can manage users (admin only)
 * Backend uses ADMIN_SYSTEM_OWNER role but enum might be ADMIN
 */
export const canManageUsers = (user: User | null): boolean => {
  if (!user) return false;
  // Handle both ADMIN and ADMIN_SYSTEM_OWNER
  return user.role === 'ADMIN' || user.role === 'ADMIN_SYSTEM_OWNER';
};

/**
 * Check if user can view dashboard
 */
export const canViewDashboard = (user: User | null): boolean => {
  return user !== null; // All authenticated users can view dashboard
};

/**
 * Get role display name
 */
export const getRoleDisplayName = (role: string): string => {
  const roleMap: Record<string, string> = {
    'ADMIN': 'VMS Admin (General Overseer)',
    'DEPARTMENT_REQUESTER': 'Department Requester',
    'FINANCE_APPROVER': 'Finance Approver',
    'COMPLIANCE_APPROVER': 'Compliance Approver'
  };
  return roleMap[role] || role;
};

