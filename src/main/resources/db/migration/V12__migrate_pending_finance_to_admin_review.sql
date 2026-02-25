-- Migrate legacy PENDING_FINANCE_REVIEW to PENDING_ADMIN_REVIEW
-- so Admin can act on them and UI shows "Pending Admin Approval" (workflow is now Compliance -> Admin only)
UPDATE vendor_creation_requests
SET status = 'PENDING_ADMIN_REVIEW'
WHERE status = 'PENDING_FINANCE_REVIEW';
