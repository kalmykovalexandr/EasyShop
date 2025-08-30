const cfg = window.EASYSHOP_CONFIG || {}
const API_BASE = cfg.API_BASE || ''
export function setToken(t){ localStorage.setItem('token', t||'') }
export function getToken(){ return localStorage.getItem('token') || '' }
export async function api(path, options={}){
  const res = await fetch(API_BASE + path, { ...options, headers: { 'Content-Type':'application/json', ...(options.headers||{}), ...(getToken()?{Authorization:'Bearer '+getToken()}:{}) } })
  const txt = await res.text(); const data = txt? JSON.parse(txt) : null
  if (!res.ok) throw new Error((data&&data.message)||res.statusText)
  return data
}
