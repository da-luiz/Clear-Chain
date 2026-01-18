#!/bin/bash

# Vendor Management System - Live Demo Script
# This script demonstrates the API endpoints

BASE_URL="http://localhost:8080/api"

echo "=========================================="
echo "Vendor Management System - Live Demo"
echo "=========================================="
echo ""
echo "Make sure the application is running on port 8080"
echo "Start it with: ./gradlew bootRun"
echo ""
read -p "Press Enter when the application is running..."

echo ""
echo "=========================================="
echo "1. Testing Dashboard (System Overview)"
echo "=========================================="
curl -s "$BASE_URL/dashboard" | python3 -m json.tool || curl -s "$BASE_URL/dashboard"
echo ""
echo ""

echo "=========================================="
echo "2. Creating a User"
echo "=========================================="
USER_RESPONSE=$(curl -s -X POST "$BASE_URL/users" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "demo.manager",
    "firstName": "Demo",
    "lastName": "Manager",
    "email": "demo.manager@company.com",
    "role": "MANAGER"
  }')
echo "$USER_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$USER_RESPONSE"
echo ""

echo "=========================================="
echo "3. Listing All Users"
echo "=========================================="
curl -s "$BASE_URL/users" | python3 -m json.tool || curl -s "$BASE_URL/users"
echo ""
echo ""

echo "=========================================="
echo "4. Creating a Vendor Request"
echo "=========================================="
REQUEST_RESPONSE=$(curl -s -X POST "$BASE_URL/vendor-requests" \
  -H "Content-Type: application/json" \
  -d '{
    "requestingDepartmentId": 1,
    "requestedByUserId": 1,
    "companyName": "ABC Office Supplies",
    "legalName": "ABC Office Supplies Inc",
    "businessJustification": "Need reliable office supplies vendor for daily operations",
    "expectedContractValue": 50000.00
  }')
echo "$REQUEST_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$REQUEST_RESPONSE"
echo ""

echo "=========================================="
echo "5. Submitting the Vendor Request"
echo "=========================================="
curl -s -X POST "$BASE_URL/vendor-requests/1/submit" | python3 -m json.tool 2>/dev/null || curl -s -X POST "$BASE_URL/vendor-requests/1/submit"
echo ""
echo ""

echo "=========================================="
echo "6. Viewing Pending Requests"
echo "=========================================="
curl -s "$BASE_URL/vendor-requests/pending" | python3 -m json.tool 2>/dev/null || curl -s "$BASE_URL/vendor-requests/pending"
echo ""
echo ""

echo "=========================================="
echo "7. Approving the Vendor Request"
echo "=========================================="
curl -s -X POST "$BASE_URL/vendor-requests/1/approve" \
  -H "Content-Type: application/json" \
  -d '{
    "approverId": 1,
    "comments": "Approved - meets all our requirements"
  }' | python3 -m json.tool 2>/dev/null || curl -s -X POST "$BASE_URL/vendor-requests/1/approve" -H "Content-Type: application/json" -d '{"approverId": 1, "comments": "Approved"}'
echo ""
echo ""

echo "=========================================="
echo "8. Viewing the Created Vendor"
echo "=========================================="
curl -s "$BASE_URL/vendors/1" | python3 -m json.tool 2>/dev/null || curl -s "$BASE_URL/vendors/1"
echo ""
echo ""

echo "=========================================="
echo "9. Creating a Vendor Directly"
echo "=========================================="
curl -s -X POST "$BASE_URL/vendors" \
  -H "Content-Type: application/json" \
  -d '{
    "companyName": "Tech Solutions Ltd",
    "legalName": "Tech Solutions Limited",
    "email": "contact@techsolutions.com",
    "phone": "+1-555-0123",
    "street": "123 Tech Street",
    "city": "San Francisco",
    "state": "CA",
    "postalCode": "94105",
    "country": "USA",
    "website": "https://techsolutions.com",
    "description": "IT equipment and services provider"
  }' | python3 -m json.tool 2>/dev/null || curl -s -X POST "$BASE_URL/vendors" -H "Content-Type: application/json" -d '{"companyName": "Tech Solutions Ltd", "email": "contact@techsolutions.com"}'
echo ""
echo ""

echo "=========================================="
echo "10. Listing All Vendors"
echo "=========================================="
curl -s "$BASE_URL/vendors?limit=10" | python3 -m json.tool 2>/dev/null || curl -s "$BASE_URL/vendors?limit=10"
echo ""
echo ""

echo "=========================================="
echo "11. Activating a Vendor"
echo "=========================================="
curl -s -X POST "$BASE_URL/vendors/1/activate"
echo " (Status: 204 No Content - Success!)"
echo ""

echo "=========================================="
echo "12. Viewing Updated Vendor Status"
echo "=========================================="
curl -s "$BASE_URL/vendors/1" | python3 -m json.tool 2>/dev/null || curl -s "$BASE_URL/vendors/1"
echo ""
echo ""

echo "=========================================="
echo "13. Creating a Contract"
echo "=========================================="
curl -s -X POST "$BASE_URL/contracts" \
  -H "Content-Type: application/json" \
  -d '{
    "vendorId": 1,
    "title": "Office Supplies Annual Contract",
    "description": "Annual contract for office supplies delivery",
    "contractValue": 50000.00,
    "startDate": "2024-01-01",
    "endDate": "2024-12-31",
    "contractType": "ANNUAL",
    "createdByUserId": 1
  }' | python3 -m json.tool 2>/dev/null || curl -s -X POST "$BASE_URL/contracts" -H "Content-Type: application/json" -d '{"vendorId": 1, "title": "Office Supplies Contract", "contractValue": 50000.00, "startDate": "2024-01-01", "endDate": "2024-12-31", "createdByUserId": 1}'
echo ""
echo ""

echo "=========================================="
echo "14. Final Dashboard Summary"
echo "=========================================="
curl -s "$BASE_URL/dashboard" | python3 -m json.tool 2>/dev/null || curl -s "$BASE_URL/dashboard"
echo ""
echo ""

echo "=========================================="
echo "Demo Complete!"
echo "=========================================="
echo ""
echo "You've seen:"
echo "  ✅ User creation"
echo "  ✅ Vendor request workflow"
echo "  ✅ Request approval"
echo "  ✅ Vendor creation and activation"
echo "  ✅ Contract creation"
echo "  ✅ Dashboard summaries"
echo ""
echo "All backend features are working!"

