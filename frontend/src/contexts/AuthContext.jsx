import React, { createContext, useContext, useState, useEffect } from 'react'
import { api, getAccessToken, setAccessToken, removeAccessToken, getRefreshToken, setRefreshToken, removeRefreshToken, getAuthorizationUrl } from '../lib/api'

const AuthContext = createContext()

export const useAuth = () => {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider')
  }
  return context
}

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  // Check if user is authenticated
  const checkAuthentication = () => {
    const token = getAccessToken()
    if (!token) return false
    
    try {
      // Decode JWT token to check expiration
      const payload = JSON.parse(atob(token.split('.')[1]))
      const now = Date.now() / 1000
      
      if (payload.exp < now) {
        // Token expired - try to refresh
        return tryRefreshToken()
      }
      
      return true
    } catch (error) {
      // Invalid token
      removeAccessToken()
      removeRefreshToken()
      setUser(null)
      return false
    }
  }

  // Try to refresh access token using refresh token
  const tryRefreshToken = async () => {
    const refreshToken = getRefreshToken()
    if (!refreshToken) {
      removeAccessToken()
      setUser(null)
      return false
    }

    try {
      const response = await fetch(`${window.EASYSHOP_CONFIG?.AUTH_SERVER_URL}/oauth2/token`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: new URLSearchParams({
          grant_type: 'refresh_token',
          client_id: 'webapp',
          refresh_token: refreshToken
        })
      })

      if (response.ok) {
        const tokenData = await response.json()
        setAccessToken(tokenData.access_token)
        if (tokenData.refresh_token) {
          setRefreshToken(tokenData.refresh_token)
        }
        return true
      } else {
        // Refresh failed
        removeAccessToken()
        removeRefreshToken()
        setUser(null)
        return false
      }
    } catch (error) {
      removeAccessToken()
      removeRefreshToken()
      setUser(null)
      return false
    }
  }

  // Get user role from token
  const getUserRole = () => {
    const token = getAccessToken()
    if (!token) return null
    
    try {
      const payload = JSON.parse(atob(token.split('.')[1]))
      return payload.role || 'USER'
    } catch (error) {
      return null
    }
  }

  // Get user email from token
  const getUserEmail = () => {
    const token = getAccessToken()
    if (!token) return null
    
    try {
      const payload = JSON.parse(atob(token.split('.')[1]))
      return payload.sub || payload.email || null
    } catch (error) {
      return null
    }
  }

  // Current authentication status
  const isAuthenticated = checkAuthentication()

  // OIDC Login function - redirects to authorization server
  const login = async (email, password) => {
    try {
      setError(null)
      // Store return URL for after login
      localStorage.setItem('oidc_return_url', window.location.pathname)
      
      // Redirect to OIDC authorization server
      const authUrl = await getAuthorizationUrl()
      window.location.href = authUrl
      
      return { success: true }
    } catch (error) {
      setError(error.message)
      return { success: false, error: error.message }
    }
  }

  // Handle OIDC callback - called from OIDCCallback component
  const handleOIDCCallback = (accessToken, refreshToken, userInfo) => {
    try {
      setAccessToken(accessToken)
      if (refreshToken) {
        setRefreshToken(refreshToken)
      }
      
      // Extract user info from token or userInfo
      const email = userInfo?.email || getUserEmail()
      const role = userInfo?.role || getUserRole() || 'USER'
      
      setUser({ email, role })
      setError(null)
      
      return { success: true }
    } catch (error) {
      setError(error.message)
      return { success: false, error: error.message }
    }
  }

  // Register function
  const register = async (email, password) => {
    try {
      setError(null)
      await api('/auth/register', {
        method: 'POST',
        body: JSON.stringify({ email, password })
      })
      return { success: true }
    } catch (error) {
      setError(error.message)
      return { success: false, error: error.message }
    }
  }

  // Logout function
  const logout = () => {
    removeAccessToken()
    removeRefreshToken()
    setUser(null)
    setError(null)
    
    // Redirect to OIDC logout endpoint
    const logoutUrl = `${window.EASYSHOP_CONFIG?.AUTH_SERVER_URL}/logout?post_logout_redirect_uri=${encodeURIComponent(window.location.origin)}`
    window.location.href = logoutUrl
  }

  // Check if user has specific role
  const hasRole = (role) => {
    if (!user) return false
    return user.role === role
  }

  // Check if user is admin
  const isAdmin = () => {
    return hasRole('ADMIN')
  }

  // Initialize auth state on app start
  useEffect(() => {
    const initAuth = async () => {
      setLoading(true)
      
      if (checkAuthentication()) {
        const email = getUserEmail()
        const role = getUserRole()
        
        if (email && role) {
          setUser({ email, role })
        } else {
          logout()
        }
      }
      
      setLoading(false)
    }

    initAuth()
  }, [])

  // Auto-logout on token expiration
  useEffect(() => {
    const checkTokenExpiration = () => {
      if (user && !checkAuthentication()) {
        logout()
      }
    }

    // Check every minute
    const interval = setInterval(checkTokenExpiration, 60000)
    
    return () => clearInterval(interval)
  }, [user])

  const value = {
    user,
    loading,
    error,
    isAuthenticated,
    login,
    register,
    logout,
    hasRole,
    isAdmin,
    handleOIDCCallback,
    clearError: () => setError(null)
  }

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  )
}
