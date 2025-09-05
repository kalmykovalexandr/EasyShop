import React, { useEffect } from 'react'
import { useAuth } from '../contexts/AuthContext'

const AuthGuard = ({ children }) => {
  const { isAuthenticated, loading, logout } = useAuth()

  useEffect(() => {
    // Check authentication status periodically
    const checkAuth = () => {
      if (!loading && !isAuthenticated) {
        // User is not authenticated, redirect to login
        window.location.href = '/account'
      }
    }

    // Check immediately
    checkAuth()

    // Check every 30 seconds
    const interval = setInterval(checkAuth, 30000)

    return () => clearInterval(interval)
  }, [isAuthenticated, loading])

  // Show loading while checking authentication
  if (loading) {
    return (
      <div style={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        height: '100vh',
        fontSize: '18px'
      }}>
        <div>Checking authentication...</div>
      </div>
    )
  }

  // If not authenticated, don't render children
  if (!isAuthenticated) {
    return null
  }

  return children
}

export default AuthGuard
