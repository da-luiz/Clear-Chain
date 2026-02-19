-- Add contact details fields (filled by Requester)
ALTER TABLE vendor_creation_requests ADD COLUMN IF NOT EXISTS primary_contact_name VARCHAR(255);
ALTER TABLE vendor_creation_requests ADD COLUMN IF NOT EXISTS primary_contact_title VARCHAR(255);
ALTER TABLE vendor_creation_requests ADD COLUMN IF NOT EXISTS primary_contact_email VARCHAR(320);
ALTER TABLE vendor_creation_requests ADD COLUMN IF NOT EXISTS primary_contact_phone VARCHAR(50);

-- Add additional company info fields (filled by Requester)
ALTER TABLE vendor_creation_requests ADD COLUMN IF NOT EXISTS business_registration_number VARCHAR(100);
ALTER TABLE vendor_creation_requests ADD COLUMN IF NOT EXISTS tax_identification_number VARCHAR(100);
ALTER TABLE vendor_creation_requests ADD COLUMN IF NOT EXISTS business_type VARCHAR(100);
ALTER TABLE vendor_creation_requests ADD COLUMN IF NOT EXISTS website VARCHAR(255);
ALTER TABLE vendor_creation_requests ADD COLUMN IF NOT EXISTS address_street VARCHAR(255);
ALTER TABLE vendor_creation_requests ADD COLUMN IF NOT EXISTS address_city VARCHAR(255);
ALTER TABLE vendor_creation_requests ADD COLUMN IF NOT EXISTS address_state VARCHAR(255);
ALTER TABLE vendor_creation_requests ADD COLUMN IF NOT EXISTS address_postal_code VARCHAR(50);
ALTER TABLE vendor_creation_requests ADD COLUMN IF NOT EXISTS address_country VARCHAR(255);

-- Add banking and payment details fields (filled by Purchasing Team)
ALTER TABLE vendor_creation_requests ADD COLUMN IF NOT EXISTS bank_name VARCHAR(255);
ALTER TABLE vendor_creation_requests ADD COLUMN IF NOT EXISTS account_holder_name VARCHAR(255);
ALTER TABLE vendor_creation_requests ADD COLUMN IF NOT EXISTS account_number VARCHAR(100);
ALTER TABLE vendor_creation_requests ADD COLUMN IF NOT EXISTS swift_bic_code VARCHAR(50);
ALTER TABLE vendor_creation_requests ADD COLUMN IF NOT EXISTS currency VARCHAR(10);
ALTER TABLE vendor_creation_requests ADD COLUMN IF NOT EXISTS payment_terms VARCHAR(100);
ALTER TABLE vendor_creation_requests ADD COLUMN IF NOT EXISTS preferred_payment_method VARCHAR(100);

-- Add category reference
ALTER TABLE vendor_creation_requests ADD COLUMN IF NOT EXISTS category_id BIGINT REFERENCES vendor_categories(id);





