import { useEffect, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { setAccessToken, setRefreshToken, removeAccessToken, removeRefreshToken } from '../lib/api'

/**
 * OIDC Callback component for handling authorization code exchange.
 * Processes the authorization code and exchanges it for access and refresh tokens.
 */
export default function OIDCCallback() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    handleCallback()
  }, [])

  const handleCallback = async () => {
    try {
      const code = searchParams.get('code')
      const state = searchParams.get('state')
      const error = searchParams.get('error')

      if (error) {
        throw new Error(`Authorization error: ${error}`)
      }

      if (!code) {
        throw new Error('No authorization code received')
      }

      // Exchange authorization code for tokens
      const tokenResponse = await exchangeCodeForTokens(code)
      
      if (tokenResponse.access_token) {
        setAccessToken(tokenResponse.access_token)
      }
      
      if (tokenResponse.refresh_token) {
        setRefreshToken(tokenResponse.refresh_token)
      }

      // Redirect to success page first, then to intended page
      const returnUrl = localStorage.getItem('oidc_return_url') || '/shop'
      localStorage.removeItem('oidc_return_url')
      localStorage.setItem('success_return_url', returnUrl)
      navigate('/success')

    } catch (err) {
      console.error('OIDC Callback Error:', err)
      setError(err.message)
      setLoading(false)
    }
  }

  const exchangeCodeForTokens = async (code) => {
    const codeVerifier = localStorage.getItem('oidc_code_verifier')
    if (!codeVerifier) {
      throw new Error('Code verifier not found')
    }

    const tokenEndpoint = `${window.EASYSHOP_CONFIG?.AUTH_SERVER_URL}/oauth2/token`
    
    const response = await fetch(tokenEndpoint, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      body: new URLSearchParams({
        grant_type: 'authorization_code',
        client_id: 'webapp',
        code: code,
        redirect_uri: window.location.origin + '/callback',
        code_verifier: codeVerifier
      })
    })

    if (!response.ok) {
      const errorData = await response.text()
      throw new Error(`Token exchange failed: ${errorData}`)
    }

    const tokenData = await response.json()
    localStorage.removeItem('oidc_code_verifier')
    
    return tokenData
  }

  if (loading) {
    return (
      <div style={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        height: '100vh',
        flexDirection: 'column',
        gap: '1rem'
      }}>
        <div>Processing authentication...</div>
        <div style={{ 
          width: '40px', 
          height: '40px', 
          border: '4px solid #f3f3f3',
          borderTop: '4px solid #3498db',
          borderRadius: '50%',
          animation: 'spin 1s linear infinite'
        }} />
        <style>{`
          @keyframes spin {
            0% { transform: rotate(0deg); }
            100% { transform: rotate(360deg); }
          }
        `}</style>
      </div>
    )
  }

  if (error) {
    return (
      <div style={{ 
        display: 'flex', 
        justifyContent: 'center', 
        alignItems: 'center', 
        height: '100vh',
        flexDirection: 'column',
        gap: '1rem',
        padding: '2rem',
        textAlign: 'center'
      }}>
        <h2>Authentication Error</h2>
        <p style={{ color: 'red' }}>{error}</p>
        <button 
          onClick={() => navigate('/account')}
          style={{
            padding: '0.5rem 1rem',
            backgroundColor: '#3498db',
            color: 'white',
            border: 'none',
            borderRadius: '4px',
            cursor: 'pointer'
          }}
        >
          Try Again
        </button>
      </div>
    )
  }

  return null
}
