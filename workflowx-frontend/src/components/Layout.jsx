import { NavLink, useNavigate } from 'react-router-dom'
import { useState } from 'react'
import { LayoutDashboard, FolderKanban, CheckSquare, Zap, Users, Bell, Settings, LogOut, Briefcase, ChevronDown, Plus } from 'lucide-react'
import useAuthStore from '../store/authStore'
import { authApi } from '../api'
import Avatar from './Avatar'
import toast from 'react-hot-toast'

const NAV = [
  { label: 'MAIN MENU', items: [
    { to: '/dashboard', icon: LayoutDashboard, label: 'Dashboard' },
    { to: '/workspaces', icon: Briefcase, label: 'Workspaces' },
    { to: '/projects', icon: FolderKanban, label: 'Projects' },
  ]},
  { label: 'WORK', items: [
    { to: '/tasks', icon: CheckSquare, label: 'My Tasks' },
    { to: '/sprints', icon: Zap, label: 'Sprints' },
    { to: '/teams', icon: Users, label: 'Teams' },
  ]},
  { label: 'ACCOUNT', items: [
    { to: '/notifications', icon: Bell, label: 'Notifications' },
    { to: '/profile', icon: Settings, label: 'Settings' },
  ]},
]

export default function Layout({ children }) {
  const { user, logout, refreshToken } = useAuthStore()
  const navigate = useNavigate()
  const [loggingOut, setLoggingOut] = useState(false)

  const handleLogout = async () => {
    setLoggingOut(true)
    try { await authApi.logout(refreshToken) } catch {}
    logout()
    navigate('/login')
    toast.success('Signed out successfully')
  }

  return (
    <div className="layout">
      {/* ── Sidebar ── */}
      <nav className="sidebar">
        <div className="sidebar-logo">
          <div className="sidebar-logo-icon">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2.5">
              <path d="M9 3H5a2 2 0 0 0-2 2v4m6-6h10a2 2 0 0 1 2 2v4M9 3v18m0 0h10a2 2 0 0 0 2-2V9M9 21H5a2 2 0 0 1-2-2V9m0 0h18"/>
            </svg>
          </div>
          <span className="sidebar-logo-text">WorkFlowX</span>
        </div>

        <div className="sidebar-nav">
          {NAV.map(section => (
            <div key={section.label}>
              <div className="sidebar-section-label">{section.label}</div>
              {section.items.map(({ to, icon: Icon, label }) => (
                <NavLink key={to} to={to} className={({ isActive }) => `sidebar-item ${isActive ? 'active' : ''}`}>
                  <Icon size={16} />
                  {label}
                </NavLink>
              ))}
            </div>
          ))}
        </div>

        <div className="sidebar-footer">
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px', padding: '6px 8px', borderRadius: 'var(--r-md)', marginBottom: '4px' }}>
            <Avatar name={user?.name} size="avatar-sm" />
            <div style={{ flex: 1, overflow: 'hidden' }}>
              <div style={{ fontSize: '13px', fontWeight: 600, color: 'rgba(255,255,255,0.9)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{user?.name}</div>
              <div style={{ fontSize: '11px', color: 'rgba(255,255,255,0.4)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{user?.email}</div>
            </div>
          </div>
          <button onClick={handleLogout} disabled={loggingOut}
            className="sidebar-item" style={{ width: '100%', color: 'rgba(255,100,100,0.8)', cursor: 'pointer' }}>
            <LogOut size={15} />
            {loggingOut ? 'Signing out…' : 'Sign out'}
          </button>
        </div>
      </nav>

      {/* ── Main ── */}
      <div className="main-content">
        <header className="topbar">
          {/* Breadcrumb / page title filled by page */}
          <div style={{ flex: 1 }} />

          {/* Search */}
          <div className="topbar-search">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/>
            </svg>
            <input type="search" placeholder="Search…" />
          </div>

          {/* Actions */}
          <div className="topbar-actions">
            <NavLink to="/notifications" className="topbar-icon-btn" style={{ position: 'relative', textDecoration: 'none', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <Bell size={18} />
              <span className="notif-dot" />
            </NavLink>
            <NavLink to="/profile" style={{ display: 'flex', alignItems: 'center', gap: '8px', padding: '5px 8px', borderRadius: 'var(--r-md)', transition: 'background var(--t-fast)', textDecoration: 'none' }}
              onMouseEnter={e => e.currentTarget.style.background = 'var(--bg-hover)'}
              onMouseLeave={e => e.currentTarget.style.background = 'transparent'}>
              <Avatar name={user?.name} size="avatar-md" />
              <div>
                <div style={{ fontSize: '13px', fontWeight: 600, color: 'var(--text-primary)', lineHeight: 1.2 }}>{user?.name?.split(' ')[0]}</div>
                <div style={{ fontSize: '11px', color: 'var(--text-subtle)', lineHeight: 1.2 }}>{user?.role}</div>
              </div>
              <ChevronDown size={14} style={{ color: 'var(--gray-300)' }} />
            </NavLink>
          </div>
        </header>

        <main className="page-content">
          {children}
        </main>
      </div>
    </div>
  )
}
