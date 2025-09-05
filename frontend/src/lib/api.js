const cfg = window.EASYSHOP_CONFIG || {}
const API_BASE = cfg.API_BASE || ''

export function getToken(){ return localStorage.getItem('token') || '' }
export function setToken(token){ localStorage.setItem('token', token) }
export function removeToken(){ localStorage.removeItem('token') }

export async function api(path, options={}){
  const token = getToken()
  
  const response = await fetch(API_BASE + path, { 
    ...options, 
    headers: { 
      'Content-Type':'application/json', 
      ...(options.headers||{}), 
      ...(token?{Authorization:'Bearer '+token}:{}) 
    } 
  })
  
  const txt = await response.text()
  const data = txt? JSON.parse(txt) : null
  
  // Handle authentication errors
  if (response.status === 401) {
    // Token expired or invalid
    removeToken()
    // Redirect to login page
    if (window.location.pathname !== '/account') {
      window.location.href = '/account'
    }
    throw new Error('Authentication required')
  }
  
  if (response.status === 403) {
    throw new Error('Access denied')
  }
  
  if (!response.ok) {
    throw new Error((data&&data.message)||response.statusText)
  }
  
  return data
}
