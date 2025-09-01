import React, { useState } from 'react'
import { api, setToken } from '../lib/api'
import { useTranslation } from 'react-i18next'

export default function Account(){
  const { t } = useTranslation()
  const [loginMsg,setLoginMsg]=useState('')
  const [regMsg,setRegMsg]=useState('')
  async function onLogin(e){
    e.preventDefault(); setLoginMsg('')
    try{ const j = await api('/auth/login',{method:'POST',body:JSON.stringify({email:e.target.email.value, password:e.target.password.value})}); setToken(j.token); setLoginMsg(t('account.login_success')) }
    catch(err){ setLoginMsg(err.message) }
  }
  async function onRegister(e){
    e.preventDefault(); setRegMsg('')
    try{ await api('/auth/register',{method:'POST',body:JSON.stringify({email:e.target.email.value, password:e.target.password.value})}); setRegMsg(t('account.register_success')) }
    catch(err){ setRegMsg(err.message) }
  }
  return (
    <div>
      <div style={{border:'1px solid #eee',borderRadius:14,padding:16,marginBottom:16}}>
        <h2>{t('account.login')}</h2>
        <form onSubmit={onLogin}>
          <input name="email" placeholder={t('account.email')} required/>
          <br/><br/>
          <input name="password" type="password" placeholder={t('account.password')} required/>
          <br/><br/>
          <button>{t('account.submit_login')}</button> <span>{loginMsg}</span>
        </form>
      </div>
      <div style={{border:'1px solid #eee',borderRadius:14,padding:16}}>
        <h2>{t('account.register')}</h2>
        <form onSubmit={onRegister}>
          <input name="email" placeholder={t('account.email')} required/>
          <br/><br/>
          <input name="password" type="password" placeholder={t('account.password_min')} minLength={8} required/>
          <br/><br/>
          <button>{t('account.create_account')}</button> <span>{regMsg}</span>
        </form>
      </div>
    </div>
  )
}
