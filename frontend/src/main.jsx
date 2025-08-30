import React from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter, Routes, Route, Link } from 'react-router-dom'
import Shop from './pages/Shop.jsx'
import Account from './pages/Account.jsx'
import Admin from './pages/Admin.jsx'
function Layout(){return (<div style={{fontFamily:'system-ui'}}><header style={{background:'#fff',borderBottom:'1px solid #eee',padding:12}}><b>EasyShop</b> — <Link to="/shop">Магазин</Link> | <Link to="/account">Аккаунт</Link> | <Link to="/admin">Админ</Link></header><div style={{maxWidth:960, margin:'0 auto', padding:16}}><Routes><Route path="/" element={<Shop/>}/><Route path="/shop" element={<Shop/>}/><Route path="/account" element={<Account/>}/><Route path="/admin" element={<Admin/>}/></Routes></div></div>)}
createRoot(document.getElementById('root')).render(<BrowserRouter><Layout/></BrowserRouter>)
