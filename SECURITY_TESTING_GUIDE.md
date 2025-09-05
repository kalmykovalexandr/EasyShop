# EasyShop Security Testing Guide

## Authentication & Authorization Testing

### 1. Frontend Security Tests

#### 1.1 Authentication Flow Testing
```bash
# Test 1: Login with valid credentials
curl -X POST http://localhost:9001/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Test123!"}'

# Test 2: Login with invalid credentials
curl -X POST http://localhost:9001/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"wrongpassword"}'

# Test 3: Register with weak password
curl -X POST http://localhost:9001/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"newuser@example.com","password":"123"}'

# Test 4: Register with strong password
curl -X POST http://localhost:9001/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"newuser@example.com","password":"StrongPass123!"}'
```

#### 1.2 Token Validation Testing
```bash
# Test 5: Access protected endpoint without token
curl -X GET http://localhost:9001/api/auth/verify

# Test 6: Access protected endpoint with valid token
curl -X GET http://localhost:9001/api/auth/verify \
  -H "Authorization: Bearer YOUR_VALID_TOKEN"

# Test 7: Access protected endpoint with expired token
curl -X GET http://localhost:9001/api/auth/verify \
  -H "Authorization: Bearer EXPIRED_TOKEN"
```

#### 1.3 Role-Based Access Testing
```bash
# Test 8: Access admin endpoint as regular user
curl -X GET http://localhost:8080/api/admin/products \
  -H "Authorization: Bearer USER_TOKEN"

# Test 9: Access admin endpoint as admin user
curl -X GET http://localhost:8080/api/admin/products \
  -H "Authorization: Bearer ADMIN_TOKEN"
```

### 2. Backend Security Tests

#### 2.1 Password Security Testing
```bash
# Test weak passwords
curl -X POST http://localhost:9001/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test1@example.com","password":"12345678"}'

curl -X POST http://localhost:9001/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test2@example.com","password":"password"}'

curl -X POST http://localhost:9001/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test3@example.com","password":"Password123"}'

# Test strong passwords
curl -X POST http://localhost:9001/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test4@example.com","password":"StrongPass123!"}'
```

#### 2.2 JWT Token Security Testing
```bash
# Test token refresh
curl -X POST http://localhost:9001/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"YOUR_REFRESH_TOKEN"}'

# Test token validation
curl -X GET http://localhost:9001/api/auth/verify \
  -H "Authorization: Bearer INVALID_TOKEN"
```

### 3. Frontend Integration Tests

#### 3.1 Route Protection Testing
1. **Test Admin Route Protection:**
   - Navigate to `/admin` without login
   - Should redirect to login page
   - Login as regular user, try to access `/admin`
   - Should show access denied message

2. **Test Authentication State:**
   - Login successfully
   - Check if user info appears in header
   - Logout and verify user info disappears

3. **Test Token Expiration:**
   - Login and wait for token to expire
   - Try to access protected resource
   - Should automatically redirect to login

#### 3.2 API Error Handling Testing
1. **Test 401 Unauthorized:**
   - Make API call without token
   - Should redirect to login page

2. **Test 403 Forbidden:**
   - Access admin resource as regular user
   - Should show access denied message

3. **Test Network Errors:**
   - Disconnect network and make API call
   - Should show appropriate error message

### 4. Security Headers Testing

#### 4.1 CORS Testing
```bash
# Test CORS preflight request
curl -X OPTIONS http://localhost:8080/api/products \
  -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: GET" \
  -H "Access-Control-Request-Headers: Authorization"
```

#### 4.2 Security Headers Verification
```bash
# Check security headers
curl -I http://localhost:8080/api/products
```

Expected headers:
- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: DENY`
- `X-XSS-Protection: 1; mode=block`

### 5. Penetration Testing Scenarios

#### 5.1 SQL Injection Testing
```bash
# Test SQL injection in login
curl -X POST http://localhost:9001/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@example.com'\'' OR 1=1--","password":"anything"}'
```

#### 5.2 XSS Testing
```bash
# Test XSS in product creation
curl -X POST http://localhost:8080/api/admin/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ADMIN_TOKEN" \
  -d '{"name":"<script>alert(\"XSS\")</script>","price":10,"stock":1}'
```

#### 5.3 CSRF Testing
```bash
# Test CSRF protection
curl -X POST http://localhost:8080/api/admin/products \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ADMIN_TOKEN" \
  -H "Origin: http://malicious-site.com" \
  -d '{"name":"Hacked Product","price":0,"stock":999}'
```

### 6. Performance Security Testing

#### 6.1 Rate Limiting Testing
```bash
# Test rate limiting on login endpoint
for i in {1..10}; do
  curl -X POST http://localhost:9001/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"email":"test@example.com","password":"wrongpassword"}'
done
```

#### 6.2 Brute Force Protection Testing
```bash
# Test brute force protection
for i in {1..20}; do
  curl -X POST http://localhost:9001/api/auth/login \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"test@example.com\",\"password\":\"wrong$i\"}"
done
```

### 7. Automated Security Testing

#### 7.1 OWASP ZAP Testing
```bash
# Run OWASP ZAP scan
zap-baseline.py -t http://localhost:8080 -r zap-report.html
```

#### 7.2 Security Headers Testing
```bash
# Test security headers with securityheaders.com
curl -I http://localhost:8080 | grep -i "x-"
```

### 8. Manual Security Checklist

#### 8.1 Authentication Security
- [ ] Strong password requirements enforced
- [ ] Password hashing with BCrypt
- [ ] JWT tokens with expiration
- [ ] Secure token storage in localStorage
- [ ] Automatic logout on token expiration
- [ ] Proper error handling for auth failures

#### 8.2 Authorization Security
- [ ] Role-based access control implemented
- [ ] Protected routes in frontend
- [ ] Admin endpoints protected
- [ ] Proper 401/403 error responses
- [ ] No privilege escalation possible

#### 8.3 Input Validation
- [ ] Email format validation
- [ ] Password strength validation
- [ ] SQL injection prevention
- [ ] XSS prevention
- [ ] CSRF protection

#### 8.4 Session Management
- [ ] Secure session handling
- [ ] Proper logout functionality
- [ ] Token refresh mechanism
- [ ] No session fixation vulnerabilities

### 9. Security Monitoring

#### 9.1 Logging
- [ ] Authentication attempts logged
- [ ] Failed login attempts logged
- [ ] Authorization failures logged
- [ ] Suspicious activity detected

#### 9.2 Monitoring
- [ ] Real-time security alerts
- [ ] Failed authentication monitoring
- [ ] Unusual access pattern detection
- [ ] Security incident response plan

## Test Results Documentation

Document all test results, including:
- Test case ID
- Test description
- Expected result
- Actual result
- Pass/Fail status
- Security issues found
- Remediation actions taken

## Security Recommendations

1. **Implement rate limiting** for authentication endpoints
2. **Add security headers** (CSP, HSTS, etc.)
3. **Implement account lockout** after failed attempts
4. **Add password reset functionality**
5. **Implement audit logging**
6. **Regular security updates**
7. **Penetration testing** by third party
8. **Security code review**
