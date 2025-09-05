import React from 'react'
import { useAuth } from '../contexts/AuthContext'

const ProtectedRoute = ({ 
  children, 
  requiredRole = null, 
  fallback = null,
  redirectTo = '/account' 
}) => {
  const { isAuthenticated, hasRole, loading } = useAuth()

  // Show loading while checking authentication
  if (loading) {
    return (
      <div style={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        height: '200px' 
      }}>
        <div>Loading...</div>
      </div>
    )
  }

  // Check if user is authenticated
  if (!isAuthenticated) {
    return fallback || (
      <div style={{ 
        textAlign: 'center', 
        padding: '40px',
        border: '1px solid #eee',
        borderRadius: '14px',
        margin: '20px'
      }}>
        <h2>Authentication Required</h2>
        <p>Please log in to access this page.</p>
        <a href="/account" style={{ 
          display: 'inline-block',
          padding: '10px 20px',
          backgroundColor: '#007bff',
          color: 'white',
          textDecoration: 'none',
          borderRadius: '5px'
        }}>
          Go to Login
        </a>
      </div>
    )
  }

  // Check if user has required role
  if (requiredRole && !hasRole(requiredRole)) {
    return fallback || (
      <div style={{ 
        textAlign: 'center', 
        padding: '40px',
        border: '1px solid #eee',
        borderRadius: '14px',
        margin: '20px'
      }}>
        <h2>Access Denied</h2>
        <p>You don't have permission to access this page.</p>
        <p>Required role: {requiredRole}</p>
        <a href="/shop" style={{ 
          display: 'inline-block',
          padding: '10px 20px',
          backgroundColor: '#007bff',
          color: 'white',
          textDecoration: 'none',
          borderRadius: '5px'
        }}>
          Go to Shop
        </a>
      </div>
    )
  }

  return children
}

export default ProtectedRoute
