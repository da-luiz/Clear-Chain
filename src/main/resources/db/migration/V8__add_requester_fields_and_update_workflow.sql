-- Add new field for supporting documents (files, links, GitHub, LinkedIn)
-- H2 doesn't support IF NOT EXISTS, so we add column separately
-- Note: If column already exists, this will fail - that's expected for idempotent migrations
ALTER TABLE vendor_creation_requests ADD COLUMN supporting_documents TEXT;

-- Note: currency column already exists, it's now used by requester instead of finance

-- Update existing statuses to new workflow
-- PENDING_FINANCE_REVIEW -> PENDING_COMPLIANCE_REVIEW (if they haven't been reviewed yet)
-- This assumes Finance hasn't reviewed them yet, so they go to Compliance first
UPDATE vendor_creation_requests 
SET status = 'PENDING_COMPLIANCE_REVIEW' 
WHERE status = 'PENDING_FINANCE_REVIEW';

-- PENDING_COMPLIANCE_REVIEW -> PENDING_FINANCE_REVIEW (if Compliance already approved, move to Finance)
-- Note: This is a data migration assumption. In practice, you may need to review each record.
-- For now, we'll leave them as is and let the new workflow handle new requests.

-- Note: H2 doesn't support COMMENT ON COLUMN, so documentation is in code comments
-- supporting_documents: JSON array of uploaded files, links, GitHub pages, LinkedIn: 
--   [{"type":"file|link|github|linkedin","value":"...","name":"...","fileName":"..."}]
--   For uploaded files: fileName is original filename, value is file path/URL
-- currency: Currency selected by requester (NGN, USD, EUR, GBP, JPY, etc.)

