import React, { useEffect, useState } from 'react'
import { useAuth } from '../contexts/AuthContext'
import { useTranslation } from 'react-i18next'
import { useNavigate } from 'react-router-dom'

export default function Success() {
  const { t } = useTranslation()
  const { user, isAuthenticated } = useAuth()
  const navigate = useNavigate()
  const [countdown, setCountdown] = useState(5)

  useEffect(() => {
    if (!isAuthenticated) {
      navigate('/account')
      return
    }

    // Get the intended return URL
    const returnUrl = localStorage.getItem('success_return_url') || '/shop'
    localStorage.removeItem('success_return_url')

    // Countdown timer to redirect to intended page
    const timer = setInterval(() => {
      setCountdown(prev => {
        if (prev <= 1) {
          navigate(returnUrl)
          return 0
        }
        return prev - 1
      })
    }, 1000)

    return () => clearInterval(timer)
  }, [isAuthenticated, navigate])

  if (!isAuthenticated) {
    return null
  }

  return (
    <div style={{
      display: 'flex',
      flexDirection: 'column',
      alignItems: 'center',
      justifyContent: 'center',
      minHeight: '60vh',
      padding: '2rem',
      textAlign: 'center'
    }}>
      <div style={{
        backgroundColor: '#d4edda',
        border: '1px solid #c3e6cb',
        borderRadius: '8px',
        padding: '2rem',
        maxWidth: '500px',
        width: '100%'
      }}>
        <div style={{
          fontSize: '4rem',
          color: '#28a745',
          marginBottom: '1rem'
        }}>
          ✅
        </div>
        
        <h1 style={{
          color: '#155724',
          marginBottom: '1rem',
          fontSize: '2rem'
        }}>
          {t('success.welcome_title', 'Welcome to EasyShop!')}
        </h1>
        
        <p style={{
          color: '#155724',
          fontSize: '1.1rem',
          marginBottom: '1.5rem'
        }}>
          {t('success.login_success', 'You have successfully logged in!')}
        </p>
        
        <div style={{
          backgroundColor: '#f8f9fa',
          border: '1px solid #dee2e6',
          borderRadius: '6px',
          padding: '1rem',
          marginBottom: '1.5rem'
        }}>
          <p style={{ margin: '0 0 0.5rem 0', fontWeight: 'bold' }}>
            {t('success.user_info', 'User Information')}:
          </p>
          <p style={{ margin: '0 0 0.5rem 0' }}>
            <strong>{t('success.email', 'Email')}:</strong> {user?.email}
          </p>
          <p style={{ margin: '0' }}>
            <strong>{t('success.role', 'Role')}:</strong> {user?.role}
          </p>
        </div>
        
        <div style={{
          display: 'flex',
          gap: '1rem',
          justifyContent: 'center',
          flexWrap: 'wrap'
        }}>
          <button
            onClick={() => navigate('/shop')}
            style={{
              backgroundColor: '#007bff',
              color: 'white',
              border: 'none',
              borderRadius: '6px',
              padding: '0.75rem 1.5rem',
              fontSize: '1rem',
              cursor: 'pointer',
              textDecoration: 'none',
              display: 'inline-block'
            }}
          >
            {t('success.go_to_shop', 'Go to Shop')}
          </button>
          
          <button
            onClick={() => navigate('/account')}
            style={{
              backgroundColor: '#6c757d',
              color: 'white',
              border: 'none',
              borderRadius: '6px',
              padding: '0.75rem 1.5rem',
              fontSize: '1rem',
              cursor: 'pointer',
              textDecoration: 'none',
              display: 'inline-block'
            }}
          >
            {t('success.account_settings', 'Account Settings')}
          </button>
        </div>
        
        <p style={{
          color: '#6c757d',
          fontSize: '0.9rem',
          marginTop: '1.5rem',
          marginBottom: '0'
        }}>
          {t('success.auto_redirect', 'You will be automatically redirected to the shop in {{countdown}} seconds...', { countdown })}
        </p>
      </div>
    </div>
  )
}
