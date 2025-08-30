import React, { useEffect, useState } from 'react'
import { api, getToken } from '../lib/api'
export default function Shop(){const [list,setList]=useState([])
  useEffect(()=>{ api('/products').then(setList).catch(e=>alert(e.message)) },[])
  async function buyOne(p){ if (!getToken()){ alert('Сначала войдите во вкладке Аккаунт'); return } try{ await api('/orders/checkout', { method:'POST', body: JSON.stringify({ items: [{ productId: p.id, quantity: 1 }] }) }); alert('Заказ оформлен') } catch(e){ alert(e.message) } }
  return (<div><h2>Каталог</h2><div style={{display:'grid',gridTemplateColumns:'repeat(auto-fill,minmax(240px,1fr))', gap:12}}>{list.map(p=>(<div key={p.id} style={{border:'1px solid #eee',borderRadius:14,padding:16}}><div style={{display:'flex',justifyContent:'space-between'}}><b>{p.name}</b><span>сток: {p.stock}</span></div><div style={{color:'#666'}}>{p.description||'—'}</div><div style={{display:'flex',justifyContent:'space-between',alignItems:'center'}}><div><b>{Number(p.price).toFixed(2)} ₽</b></div><button onClick={()=>buyOne(p)}>Купить 1 шт.</button></div></div>))}</div></div>) }
