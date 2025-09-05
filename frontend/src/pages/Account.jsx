import React, { useState, useEffect } from 'react'
import { useAuth } from '../contexts/AuthContext'
import { useTranslation } from 'react-i18next'

export default function Account(){
  const { t } = useTranslation()
  const { user, login, register, logout, error, clearError } = useAuth()
  const [loginMsg, setLoginMsg] = useState('')
  const [regMsg, setRegMsg] = useState('')
  const [isLoading, setIsLoading] = useState(false)

  // Clear messages when error changes
  useEffect(() => {
    if (error) {
      setLoginMsg('')
      setRegMsg('')
    }
  }, [error])

  async function onLogin(e){
    e.preventDefault()
    setLoginMsg('')
    clearError()
    setIsLoading(true)
    
    const result = await login(e.target.email.value, e.target.password.value)
    
    if (result.success) {
      setLoginMsg(t('account.login_success'))
      e.target.reset()
    } else {
      setLoginMsg(result.error)
    }
    
    setIsLoading(false)
  }

  async function onRegister(e){
    e.preventDefault()
    setRegMsg('')
    clearError()
    setIsLoading(true)
    
    const result = await register(e.target.email.value, e.target.password.value)
    
    if (result.success) {
      setRegMsg(t('account.register_success'))
      e.target.reset()
    } else {
      setRegMsg(result.error)
    }
    
    setIsLoading(false)
  }
  // If user is logged in, show user info and logout
  if (user) {
    return (
      <div>
        <div style={{border:'1px solid #eee',borderRadius:14,padding:16,marginBottom:16}}>
          <h2>Welcome, {user.email}!</h2>
          <p>Role: {user.role}</p>
          <button 
            onClick={logout}
            style={{
              padding: '10px 20px',
              backgroundColor: '#dc3545',
              color: 'white',
              border: 'none',
              borderRadius: '5px',
              cursor: 'pointer'
            }}
          >
            Logout
          </button>
        </div>
      </div>
    )
  }

  return (
    <div>
      {error && (
        <div style={{
          backgroundColor: '#f8d7da',
          color: '#721c24',
          padding: '10px',
          borderRadius: '5px',
          marginBottom: '16px',
          border: '1px solid #f5c6cb'
        }}>
          {error}
        </div>
      )}
      
      <div style={{border:'1px solid #eee',borderRadius:14,padding:16,marginBottom:16}}>
        <h2>{t('account.login')}</h2>
        <form onSubmit={onLogin}>
          <input 
            name="email" 
            type="email"
            placeholder={t('account.email')} 
            required
            disabled={isLoading}
          />
          <br/><br/>
          <input 
            name="password" 
            type="password" 
            placeholder={t('account.password')} 
            required
            disabled={isLoading}
          />
          <br/><br/>
          <button disabled={isLoading}>
            {isLoading ? 'Logging in...' : t('account.submit_login')}
          </button> 
          <span style={{color: loginMsg.includes('success') ? 'green' : 'red'}}>
            {loginMsg}
          </span>
        </form>
      </div>
      
      <div style={{border:'1px solid #eee',borderRadius:14,padding:16}}>
        <h2>{t('account.register')}</h2>
        <form onSubmit={onRegister}>
          <input 
            name="email" 
            type="email"
            placeholder={t('account.email')} 
            required
            disabled={isLoading}
          />
          <br/><br/>
          <input 
            name="password" 
            type="password" 
            placeholder={t('account.password_min')} 
            minLength={8} 
            required
            disabled={isLoading}
          />
          <br/><br/>
          <button disabled={isLoading}>
            {isLoading ? 'Registering...' : t('account.create_account')}
          </button> 
          <span style={{color: regMsg.includes('success') ? 'green' : 'red'}}>
            {regMsg}
          </span>
        </form>
      </div>
    </div>
  )
}
