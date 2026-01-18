-- Insert realistic vendor categories for businesses
-- Delete any existing categories first (in case of re-run)
DELETE FROM vendor_categories;

-- Insert comprehensive vendor categories
INSERT INTO vendor_categories (id, name, code, description, is_active, created_at) VALUES
(1, 'IT Services & Software', 'IT_SERVICES', 'Information Technology services, software development, cloud services, IT consulting', true, CURRENT_TIMESTAMP),
(2, 'Food & Catering', 'FOOD', 'Food suppliers, catering services, restaurant services, food delivery', true, CURRENT_TIMESTAMP),
(3, 'Office Supplies & Equipment', 'OFFICE_SUPPLIES', 'Office furniture, stationery, printers, office equipment', true, CURRENT_TIMESTAMP),
(4, 'Appliances & Electronics', 'APPLIANCES', 'Home appliances, office electronics, kitchen equipment, HVAC systems', true, CURRENT_TIMESTAMP),
(5, 'Facilities & Maintenance', 'FACILITIES', 'Building maintenance, janitorial services, landscaping, security services', true, CURRENT_TIMESTAMP),
(6, 'Professional Services', 'PROFESSIONAL', 'Legal services, accounting, consulting, marketing, HR services', true, CURRENT_TIMESTAMP),
(7, 'Transportation & Logistics', 'TRANSPORT', 'Shipping, courier services, freight, vehicle rental, logistics', true, CURRENT_TIMESTAMP),
(8, 'Healthcare & Medical', 'HEALTHCARE', 'Medical equipment, pharmaceutical supplies, healthcare services', true, CURRENT_TIMESTAMP),
(9, 'Construction & Building', 'CONSTRUCTION', 'Construction materials, contractors, building services, renovation', true, CURRENT_TIMESTAMP),
(10, 'Manufacturing & Industrial', 'MANUFACTURING', 'Manufacturing equipment, industrial supplies, raw materials', true, CURRENT_TIMESTAMP),
(11, 'Telecommunications', 'TELECOM', 'Internet services, phone services, network equipment, communication services', true, CURRENT_TIMESTAMP),
(12, 'Utilities & Energy', 'UTILITIES', 'Electricity, water, gas, renewable energy, utility services', true, CURRENT_TIMESTAMP),
(13, 'Marketing & Advertising', 'MARKETING', 'Advertising agencies, marketing services, promotional materials, digital marketing', true, CURRENT_TIMESTAMP),
(14, 'Financial Services', 'FINANCIAL', 'Banking services, insurance, financial consulting, payment processing', true, CURRENT_TIMESTAMP),
(15, 'Education & Training', 'EDUCATION', 'Training services, educational materials, e-learning platforms, certification programs', true, CURRENT_TIMESTAMP);



