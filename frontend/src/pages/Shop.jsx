import React, { useEffect, useState } from 'react'
import { api, getToken } from '../lib/api'
import { useTranslation } from 'react-i18next'

export default function Shop(){
  const { t } = useTranslation()
  const [list,setList]=useState([])
  const [loading,setLoading]=useState(true)
  const [error,setError]=useState(null)
  
  useEffect(()=>{ 
    setLoading(true)
    setError(null)
    api('/products')
      .then(setList)
      .catch(e=>{
        console.error('Failed to load products:', e)
        setError(e.message)
      })
      .finally(()=>setLoading(false))
  },[])
  async function buyOne(p){
    if (!getToken()){ alert(t('shop.login_first')); return }
    try{ await api('/purchases/checkout', { method:'POST', body: JSON.stringify({ items: [{ productId: p.id, quantity: 1 }] }) }); alert(t('shop.order_done')) }
    catch(e){ alert(e.message) }
  }
  if (loading) {
    return (
      <div style={{textAlign: 'center', padding: '40px'}}>
        <h2>{t('shop.catalog')}</h2>
        <div>{t('shop.loading')}</div>
      </div>
    )
  }

  if (error) {
    return (
      <div style={{textAlign: 'center', padding: '40px'}}>
        <h2>{t('shop.catalog')}</h2>
        <div style={{color: 'red', marginBottom: '20px'}}>
          Error loading products: {error}
        </div>
        <button onClick={() => window.location.reload()}>
          {t('shop.retry')}
        </button>
      </div>
    )
  }

  return (
    <div>
      <h2>{t('shop.catalog')}</h2>
      {list.length === 0 ? (
        <div style={{textAlign: 'center', padding: '40px', color: '#666'}}>
          {t('shop.no_products')}
        </div>
      ) : (
        <div style={{display:'grid',gridTemplateColumns:'repeat(auto-fill,minmax(240px,1fr))', gap:12}}>
          {list.map(p=>(
            <div key={p.id} style={{border:'1px solid #eee',borderRadius:14,padding:16}}>
              <div style={{display:'flex',justifyContent:'space-between'}}><b>{p.name}</b><span>{t('shop.stock')}: {p.stock}</span></div>
              <div style={{color:'#666'}}>{p.description||'—'}</div>
              <div style={{display:'flex',justifyContent:'space-between',alignItems:'center'}}>
                <div><b>{Number(p.price).toFixed(2)} ₽</b></div>
                <button onClick={()=>buyOne(p)}>{t('shop.buy_one')}</button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
