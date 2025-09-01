import React from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter, Routes, Route, Link } from 'react-router-dom'
import i18n from 'i18next'
import { I18nextProvider, useTranslation } from 'react-i18next'
import en from './i18n/en.json'
import ru from './i18n/ru.json'
import Shop from './pages/Shop.jsx'
import Account from './pages/Account.jsx'
import Admin from './pages/Admin.jsx'

i18n.init({ resources:{ en:{ translation:en }, ru:{ translation:ru } }, lng:localStorage.getItem('lang')||'ru', interpolation:{ escapeValue:false } })

function Layout(){const { t,i18n } = useTranslation(); function change(e){ const l=e.target.value; i18n.changeLanguage(l); localStorage.setItem('lang',l) } return (<div style={{fontFamily:'system-ui'}}><header style={{background:'#fff',borderBottom:'1px solid #eee',padding:12}}><b>EasyShop</b> — <Link to="/shop">{t('nav.shop')}</Link> | <Link to="/account">{t('nav.account')}</Link> | <Link to="/admin">{t('nav.admin')}</Link> <select value={i18n.language} onChange={change}><option value="ru">RU</option><option value="en">EN</option></select></header><div style={{maxWidth:960, margin:'0 auto', padding:16}}><Routes><Route path="/" element={<Shop/>}/><Route path="/shop" element={<Shop/>}/><Route path="/account" element={<Account/>}/><Route path="/admin" element={<Admin/>}/></Routes></div></div>)}

createRoot(document.getElementById('root')).render(<I18nextProvider i18n={i18n}><BrowserRouter><Layout/></BrowserRouter></I18nextProvider>)
