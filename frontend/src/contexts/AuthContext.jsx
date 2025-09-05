import React, { createContext, useContext, useState, useEffect } from 'react'
import { api, getToken, setToken, removeToken } from '../lib/api'

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
    const token = getToken()
    if (!token) return false
    
    try {
      // Decode JWT token to check expiration
      const payload = JSON.parse(atob(token.split('.')[1]))
      const now = Date.now() / 1000
      
      if (payload.exp < now) {
        // Token expired
        removeToken()
        setUser(null)
        return false
      }
      
      return true
    } catch (error) {
      // Invalid token
      removeToken()
      setUser(null)
      return false
    }
  }

  // Current authentication status
  const isAuthenticated = checkAuthentication()

  // Get user role from token
  const getUserRole = () => {
    const token = getToken()
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
    const token = getToken()
    if (!token) return null
    
    try {
      const payload = JSON.parse(atob(token.split('.')[1]))
      return payload.sub || null
    } catch (error) {
      return null
    }
  }

  // Login function
  const login = async (email, password) => {
    try {
      setError(null)
      const response = await api('/auth/login', {
        method: 'POST',
        body: JSON.stringify({ email, password })
      })
      
      if (response.token) {
        setToken(response.token)
        setUser({
          email: response.email,
          role: response.role
        })
        return { success: true }
      } else {
        throw new Error('Invalid response from server')
      }
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
    removeToken()
    setUser(null)
    setError(null)
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
      
      if (isAuthenticated) {
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
    clearError: () => setError(null)
  }

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  )
}
