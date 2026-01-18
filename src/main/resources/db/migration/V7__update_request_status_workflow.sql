-- Update existing vendor creation request statuses to new workflow
-- Map old statuses to new workflow statuses

-- SUBMITTED -> PENDING_FINANCE_REVIEW (first step in new workflow)
UPDATE vendor_creation_requests 
SET status = 'PENDING_FINANCE_REVIEW' 
WHERE status = 'SUBMITTED';

-- UNDER_REVIEW -> PENDING_FINANCE_REVIEW (assume they're pending finance review)
UPDATE vendor_creation_requests 
SET status = 'PENDING_FINANCE_REVIEW' 
WHERE status = 'UNDER_REVIEW';

-- APPROVED -> ACTIVE (if approved, vendor should be active)
UPDATE vendor_creation_requests 
SET status = 'ACTIVE' 
WHERE status = 'APPROVED';

-- REJECTED -> REJECTED_BY_FINANCE (default to finance rejection)
UPDATE vendor_creation_requests 
SET status = 'REJECTED_BY_FINANCE' 
WHERE status = 'REJECTED';

-- RETURNED_FOR_INFO -> PENDING_FINANCE_REVIEW (put back in queue)
UPDATE vendor_creation_requests 
SET status = 'PENDING_FINANCE_REVIEW' 
WHERE status = 'RETURNED_FOR_INFO';

-- CANCELLED remains CANCELLED (no change)
-- DRAFT remains DRAFT (no change)


