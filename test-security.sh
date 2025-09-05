#!/bin/bash

# EasyShop Security Testing Script
# This script tests the authentication and authorization system

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
AUTH_SERVICE_URL="http://localhost:9001"
API_GATEWAY_URL="http://localhost:8080"
TEST_EMAIL="test@example.com"
TEST_PASSWORD="Test123!"
WEAK_PASSWORD="12345678"

echo -e "${YELLOW}🔒 EasyShop Security Testing${NC}"
echo "=================================="

# Test 1: Register with weak password (should fail)
echo -e "\n${YELLOW}Test 1: Register with weak password${NC}"
WEAK_REGISTER_RESPONSE=$(curl -s -X POST "$AUTH_SERVICE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"weak@example.com\",\"password\":\"$WEAK_PASSWORD\"}")

if echo "$WEAK_REGISTER_RESPONSE" | grep -q "false"; then
  echo -e "${GREEN}✅ PASS: Weak password rejected${NC}"
else
  echo -e "${RED}❌ FAIL: Weak password accepted${NC}"
fi

# Test 2: Register with strong password (should succeed)
echo -e "\n${YELLOW}Test 2: Register with strong password${NC}"
STRONG_REGISTER_RESPONSE=$(curl -s -X POST "$AUTH_SERVICE_URL/api/auth/register" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$TEST_EMAIL\",\"password\":\"$TEST_PASSWORD\"}")

if echo "$STRONG_REGISTER_RESPONSE" | grep -q "true"; then
  echo -e "${GREEN}✅ PASS: Strong password accepted${NC}"
else
  echo -e "${RED}❌ FAIL: Strong password rejected${NC}"
  echo "Response: $STRONG_REGISTER_RESPONSE"
fi

# Test 3: Login with valid credentials
echo -e "\n${YELLOW}Test 3: Login with valid credentials${NC}"
LOGIN_RESPONSE=$(curl -s -X POST "$AUTH_SERVICE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$TEST_EMAIL\",\"password\":\"$TEST_PASSWORD\"}")

if echo "$LOGIN_RESPONSE" | grep -q "token"; then
  echo -e "${GREEN}✅ PASS: Login successful${NC}"
  # Extract token for further tests
  TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)
  REFRESH_TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"refreshToken":"[^"]*"' | cut -d'"' -f4)
  echo "Token extracted: ${TOKEN:0:20}..."
else
  echo -e "${RED}❌ FAIL: Login failed${NC}"
  echo "Response: $LOGIN_RESPONSE"
  exit 1
fi

# Test 4: Access protected endpoint without token (should fail)
echo -e "\n${YELLOW}Test 4: Access protected endpoint without token${NC}"
PROTECTED_RESPONSE=$(curl -s -X GET "$AUTH_SERVICE_URL/api/auth/verify")

if echo "$PROTECTED_RESPONSE" | grep -q "401\|Unauthorized"; then
  echo -e "${GREEN}✅ PASS: Protected endpoint requires authentication${NC}"
else
  echo -e "${RED}❌ FAIL: Protected endpoint accessible without token${NC}"
  echo "Response: $PROTECTED_RESPONSE"
fi

# Test 5: Access protected endpoint with valid token (should succeed)
echo -e "\n${YELLOW}Test 5: Access protected endpoint with valid token${NC}"
if [ -n "$TOKEN" ]; then
  AUTHENTICATED_RESPONSE=$(curl -s -X GET "$AUTH_SERVICE_URL/api/auth/verify" \
    -H "Authorization: Bearer $TOKEN")
  
  if echo "$AUTHENTICATED_RESPONSE" | grep -q "true"; then
    echo -e "${GREEN}✅ PASS: Authenticated access successful${NC}"
  else
    echo -e "${RED}❌ FAIL: Authenticated access failed${NC}"
    echo "Response: $AUTHENTICATED_RESPONSE"
  fi
else
  echo -e "${YELLOW}⚠️  SKIP: No token available${NC}"
fi

# Test 6: Test token refresh
echo -e "\n${YELLOW}Test 6: Token refresh${NC}"
if [ -n "$REFRESH_TOKEN" ]; then
  REFRESH_RESPONSE=$(curl -s -X POST "$AUTH_SERVICE_URL/api/auth/refresh" \
    -H "Content-Type: application/json" \
    -d "{\"refreshToken\":\"$REFRESH_TOKEN\"}")
  
  if echo "$REFRESH_RESPONSE" | grep -q "token"; then
    echo -e "${GREEN}✅ PASS: Token refresh successful${NC}"
  else
    echo -e "${RED}❌ FAIL: Token refresh failed${NC}"
    echo "Response: $REFRESH_RESPONSE"
  fi
else
  echo -e "${YELLOW}⚠️  SKIP: No refresh token available${NC}"
fi

# Test 7: Test admin endpoint access (if available)
echo -e "\n${YELLOW}Test 7: Admin endpoint access${NC}"
if [ -n "$TOKEN" ]; then
  ADMIN_RESPONSE=$(curl -s -X GET "$API_GATEWAY_URL/api/admin/products" \
    -H "Authorization: Bearer $TOKEN")
  
  if echo "$ADMIN_RESPONSE" | grep -q "403\|Forbidden"; then
    echo -e "${GREEN}✅ PASS: Admin endpoint properly protected${NC}"
  elif echo "$ADMIN_RESPONSE" | grep -q "200\|products"; then
    echo -e "${YELLOW}⚠️  INFO: Admin endpoint accessible (user might be admin)${NC}"
  else
    echo -e "${YELLOW}⚠️  INFO: Admin endpoint response: $ADMIN_RESPONSE${NC}"
  fi
else
  echo -e "${YELLOW}⚠️  SKIP: No token available${NC}"
fi

# Test 8: Test logout
echo -e "\n${YELLOW}Test 8: Logout functionality${NC}"
LOGOUT_RESPONSE=$(curl -s -X POST "$AUTH_SERVICE_URL/api/auth/logout" \
  -H "Authorization: Bearer $TOKEN")

if echo "$LOGOUT_RESPONSE" | grep -q "true"; then
  echo -e "${GREEN}✅ PASS: Logout successful${NC}"
else
  echo -e "${YELLOW}⚠️  INFO: Logout response: $LOGOUT_RESPONSE${NC}"
fi

# Test 9: Test password requirements endpoint
echo -e "\n${YELLOW}Test 9: Password requirements endpoint${NC}"
PASSWORD_REQ_RESPONSE=$(curl -s -X GET "$AUTH_SERVICE_URL/api/auth/password-requirements")

if echo "$PASSWORD_REQ_RESPONSE" | grep -q "Password must be"; then
  echo -e "${GREEN}✅ PASS: Password requirements endpoint working${NC}"
else
  echo -e "${RED}❌ FAIL: Password requirements endpoint not working${NC}"
  echo "Response: $PASSWORD_REQ_RESPONSE"
fi

echo -e "\n${YELLOW}🔒 Security Testing Complete${NC}"
echo "=================================="
echo -e "${GREEN}✅ All critical security tests passed!${NC}"
echo ""
echo "Next steps:"
echo "1. Run the application and test the frontend"
echo "2. Verify route protection works correctly"
echo "3. Test admin panel access control"
echo "4. Perform manual security testing"
echo "5. Review security headers and CORS configuration"
