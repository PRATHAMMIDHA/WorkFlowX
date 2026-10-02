import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { authApi } from '../api'
import useAuthStore from '../store/authStore'
import { handleApiError } from '../utils/helpers'
import toast from 'react-hot-toast'
import { Eye, EyeOff, CheckCircle, Zap, Users, Shield } from 'lucide-react'

const FEATURES = [
  { icon: CheckCircle, text: 'Plan and track work across your team' },
  { icon: Zap, text: 'Agile sprints with powerful Kanban boards' },
  { icon: Users, text: 'Collaborate in real time with your team' },
  { icon: Shield, text: 'Role-based permissions and secure access' },
]

export default function LoginPage() {
  const navigate = useNavigate()
  const login = useAuthStore(s => s.login)
  const [form, setForm] = useState({ email: '', password: '' })
  const [loading, setLoading] = useState(false)
  const [showPw, setShowPw] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      const res = await authApi.login(form)
      login(res.data.data)
      toast.success(`Welcome back, ${res.data.data.name.split(' ')[0]}!`)
      navigate('/dashboard')
    } catch (err) {
      toast.error(handleApiError(err))
    } finally { setLoading(false) }
  }

  return (
    <div className="auth-page">
      {/* Left decorative panel */}
      <div className="auth-panel-left">
        <div className="auth-logo">
          <div className="auth-logo-icon">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2.5">
              <path d="M9 3H5a2 2 0 0 0-2 2v4m6-6h10a2 2 0 0 1 2 2v4M9 3v18m0 0h10a2 2 0 0 0 2-2V9M9 21H5a2 2 0 0 1-2-2V9m0 0h18"/>
            </svg>
          </div>
          <span className="auth-logo-text" style={{ color: '#fff' }}>WorkFlowX</span>
        </div>
        <h1 className="auth-panel-headline">
          Your team's<br/><span>command center</span>
        </h1>
        <p className="auth-panel-desc">
          Everything your team needs to plan, build, and ship — in one powerful platform.
        </p>
        <ul className="auth-panel-features">
          {FEATURES.map(({ icon: Icon, text }) => (
            <li key={text}><Icon size={16} /> {text}</li>
          ))}
        </ul>
      </div>

      {/* Right — login form */}
      <div className="auth-panel-right">
        <div className="auth-card">
          <div className="auth-logo">
            <div className="auth-logo-icon">
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2.5">
                <path d="M9 3H5a2 2 0 0 0-2 2v4m6-6h10a2 2 0 0 1 2 2v4M9 3v18m0 0h10a2 2 0 0 0 2-2V9M9 21H5a2 2 0 0 1-2-2V9m0 0h18"/>
              </svg>
            </div>
            <span className="auth-logo-text">WorkFlowX</span>
          </div>

          <h2 className="auth-title">Sign in to your account</h2>
          <p className="auth-subtitle">Welcome back — let's pick up where you left off</p>

          <form className="auth-form" onSubmit={handleSubmit}>
            <div className="form-group">
              <label className="form-label">Email address</label>
              <input type="email" className="input" placeholder="alice@company.com"
                value={form.email} onChange={e => setForm({ ...form, email: e.target.value })}
                required autoComplete="email" />
            </div>
            <div className="form-group">
              <label className="form-label">Password</label>
              <div style={{ position: 'relative' }}>
                <input type={showPw ? 'text' : 'password'} className="input" placeholder="Your password"
                  style={{ paddingRight: '40px' }}
                  value={form.password} onChange={e => setForm({ ...form, password: e.target.value })}
                  required autoComplete="current-password" />
                <button type="button" onClick={() => setShowPw(!showPw)}
                  style={{ position: 'absolute', right: '10px', top: '50%', transform: 'translateY(-50%)', color: 'var(--gray-400)', background: 'none', border: 'none', cursor: 'pointer', display: 'flex' }}>
                  {showPw ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              </div>
            </div>
            <button type="submit" className="btn btn-primary w-full btn-lg" style={{ justifyContent: 'center', marginTop: '4px' }} disabled={loading}>
              {loading ? <div className="spinner spinner-sm" style={{ borderTopColor: 'white' }} /> : null}
              {loading ? 'Signing in…' : 'Sign in'}
            </button>

            <div className="auth-divider">or</div>

            <button type="button" className="btn btn-default w-full" style={{ justifyContent: 'center' }}
              onClick={() => setForm({ email: 'alice@test.com', password: 'Test1234!' })}>
              Fill demo credentials
            </button>
          </form>

          <div className="auth-footer">
            Don&apos;t have an account?{' '}
            <Link to="/register" className="auth-link">Sign up free</Link>
          </div>
        </div>
      </div>
    </div>
  )
}
