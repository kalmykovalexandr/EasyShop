import React, { useEffect, useState } from 'react'
import { api } from '../lib/api'
import { useTranslation } from 'react-i18next'
import ProtectedRoute from '../components/ProtectedRoute'

function AdminContent(){
  const { t } = useTranslation()
  const [list,setList]=useState([])
  async function load(){ try{ const l = await api('/products'); setList(l) } catch(e){ alert(e.message) } }
  useEffect(()=>{ load() },[])
  async function create(e){
    e.preventDefault()
    const data = { name:e.target.name.value, description:e.target.description.value, price:Number(e.target.price.value), stock:Number(e.target.stock.value) }
    try{ await api('/admin/products',{ method:'POST', body:JSON.stringify(data) }); e.target.reset(); load() }
    catch(e){ alert(e.message) }
  }
  async function del(id){
    if(!confirm(t('admin.confirm_delete'))) return
    try{ await api('/admin/products/'+id,{ method:'DELETE' }); load() }
    catch(e){ alert(e.message) }
  }
  return (
    <div>
      <div style={{border:'1px solid #eee',borderRadius:14,padding:16, marginBottom:16}}>
        <h2>{t('admin.create_product')}</h2>
        <form onSubmit={create}>
          <input name="name" placeholder={t('admin.name')} required/> <input name="price" type="number" step="0.01" defaultValue="0"/> <input name="stock" type="number" defaultValue="0"/>
          <br/><br/>
          <textarea name="description" placeholder={t('admin.description')} rows="3" style={{width:'100%'}}></textarea>
          <br/><br/>
          <button>{t('admin.create')}</button>
        </form>
      </div>
      <div style={{border:'1px solid #eee',borderRadius:14,padding:16}}>
        <h2>{t('admin.catalog')}</h2>
        <table style={{width:'100%',borderCollapse:'collapse'}}>
          <thead>
            <tr><th>{t('admin.name')}</th><th>{t('admin.price')}</th><th>{t('admin.stock')}</th><th></th></tr>
          </thead>
          <tbody>
            {list.map(p=>(
              <tr key={p.id}>
                <td>{p.name}</td>
                <td>{Number(p.price).toFixed(2)} ₽</td>
                <td>{p.stock}</td>
                <td style={{textAlign:'right'}}><button onClick={()=>del(p.id)}>{t('admin.delete')}</button></td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}

export default function Admin(){
  return (
    <ProtectedRoute requiredRole="ADMIN">
      <AdminContent />
    </ProtectedRoute>
  )
}
