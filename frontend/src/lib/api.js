const cfg = window.EASYSHOP_CONFIG || {}
const API_BASE = cfg.API_BASE || ''

// OIDC Configuration
export const OIDC_CONFIG = {
  authority: cfg.AUTH_SERVER_URL || 'http://localhost:9001',
  client_id: 'webapp',
  redirect_uri: window.location.origin + '/callback',
  response_type: 'code',
  scope: 'openid profile read write',
  post_logout_redirect_uri: window.location.origin,
  automaticSilentRenew: true,
  loadUserInfo: true,
  pkce: true
}

// OIDC Token Management
export function getAccessToken() {
  return localStorage.getItem('oidc_access_token') || ''
}

export function setAccessToken(token) {
  localStorage.setItem('oidc_access_token', token)
}

export function removeAccessToken() {
  localStorage.removeItem('oidc_access_token')
}

export function getRefreshToken() {
  return localStorage.getItem('oidc_refresh_token') || ''
}

export function setRefreshToken(token) {
  localStorage.setItem('oidc_refresh_token', token)
}

export function removeRefreshToken() {
  localStorage.removeItem('oidc_refresh_token')
}

// Legacy token functions for backward compatibility
export function getToken() { return getAccessToken() }
export function setToken(token) { setAccessToken(token) }
export function removeToken() { 
  removeAccessToken()
  removeRefreshToken()
}

// API call with OIDC token
export async function api(path, options = {}) {
  const token = getAccessToken()
  
  const response = await fetch(API_BASE + path, { 
    ...options, 
    headers: { 
      'Content-Type': 'application/json', 
      ...(options.headers || {}), 
      ...(token ? { Authorization: 'Bearer ' + token } : {}) 
    } 
  })
  
  const txt = await response.text()
  const data = txt ? JSON.parse(txt) : null
  
  // Handle authentication errors
  if (response.status === 401) {
    // Token expired or invalid - OIDC library will handle refresh
    removeAccessToken()
    removeRefreshToken()
    // Redirect to login page if not on public page
    const publicPaths = ['/', '/shop', '/account', '/callback']
    if (!publicPaths.includes(window.location.pathname)) {
      window.location.href = '/account'
    }
    throw new Error('Authentication required')
  }
  
  if (response.status === 403) {
    throw new Error('Access denied')
  }
  
  if (!response.ok) {
    throw new Error((data && data.message) || response.statusText)
  }
  
  return data
}

// OIDC Discovery endpoint
export function getOIDCDiscoveryUrl() {
  return `${OIDC_CONFIG.authority}/.well-known/openid-configuration`
}

// OIDC Authorization URL with PKCE
export async function getAuthorizationUrl() {
  const codeChallenge = await generateCodeChallenge()
  const params = new URLSearchParams({
    client_id: OIDC_CONFIG.client_id,
    redirect_uri: OIDC_CONFIG.redirect_uri,
    response_type: OIDC_CONFIG.response_type,
    scope: OIDC_CONFIG.scope,
    state: generateRandomString(32),
    code_challenge: codeChallenge,
    code_challenge_method: 'S256'
  })
  
  return `${OIDC_CONFIG.authority}/oauth2/authorize?${params.toString()}`
}

// PKCE Code Challenge Generation
async function generateCodeChallenge() {
  const codeVerifier = generateRandomString(128)
  localStorage.setItem('oidc_code_verifier', codeVerifier)
  
  const hash = await sha256(codeVerifier)
  return hash
}

// Generate random string for PKCE
function generateRandomString(length) {
  const charset = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~'
  let result = ''
  for (let i = 0; i < length; i++) {
    result += charset.charAt(Math.floor(Math.random() * charset.length))
  }
  return result
}

// SHA256 hash function for PKCE
async function sha256(message) {
  // Check if crypto.subtle is available (requires HTTPS)
  if (crypto.subtle) {
    const msgBuffer = new TextEncoder().encode(message)
    const hashBuffer = await crypto.subtle.digest('SHA-256', msgBuffer)
    return btoa(String.fromCharCode(...new Uint8Array(hashBuffer)))
      .replace(/\+/g, '-')
      .replace(/\//g, '_')
      .replace(/=/g, '')
  } else {
    // Fallback for HTTP - use a simple hash (not cryptographically secure)
    console.warn('crypto.subtle not available, using fallback hash')
    return btoa(message)
      .replace(/\+/g, '-')
      .replace(/\//g, '_')
      .replace(/=/g, '')
  }
}
