import { useState, useEffect } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { userApi } from '../api'
import useAuthStore from '../store/authStore'
import { LoadingPage } from '../components/Spinner'
import Avatar from '../components/Avatar'
import { handleApiError, formatDate } from '../utils/helpers'
import toast from 'react-hot-toast'
import { User, Lock, Save, Shield, CheckCircle } from 'lucide-react'
import { Link } from 'react-router-dom'

export default function ProfilePage() {
  const { user, updateUser } = useAuthStore()
  const qc = useQueryClient()
  const [activeTab, setActiveTab] = useState('profile')

  const { data, isLoading } = useQuery({ queryKey: ['me'], queryFn: () => userApi.getMe() })
  const profile = data?.data?.data

  const updateProfile = useMutation({
    mutationFn: (data) => userApi.updateProfile(data),
    onSuccess: (res) => {
      const updated = res.data.data
      updateUser({ name: updated.name, bio: updated.bio, avatarUrl: updated.avatarUrl })
      qc.invalidateQueries({ queryKey: ['me'] })
      toast.success('Profile updated successfully')
    },
    onError: (err) => toast.error(handleApiError(err)),
  })

  const changePassword = useMutation({
    mutationFn: (data) => userApi.changePassword(data),
    onSuccess: () => {
      toast.success('Password changed successfully')
      setPwForm({ currentPassword: '', newPassword: '', confirmPassword: '' })
    },
    onError: (err) => toast.error(handleApiError(err)),
  })

  const [form, setForm] = useState({ name: '', bio: '', avatarUrl: '' })
  const [pwForm, setPwForm] = useState({ currentPassword: '', newPassword: '', confirmPassword: '' })

  useEffect(() => {
    if (profile) {
      setForm({
        name: profile.name || '',
        bio: profile.bio || '',
        avatarUrl: profile.avatarUrl || ''
      })
    }
  }, [profile])

  if (isLoading) return <LoadingPage />

  const handleProfileSubmit = (e) => {
    e.preventDefault()
    updateProfile.mutate(form)
  }

  const handlePasswordSubmit = (e) => {
    e.preventDefault()
    if (pwForm.newPassword !== pwForm.confirmPassword) {
      toast.error('Passwords do not match')
      return
    }
    changePassword.mutate({
      currentPassword: pwForm.currentPassword,
      newPassword: pwForm.newPassword
    })
  }

  return (
    <div style={{ maxWidth: '680px' }}>
      <div className="page-header">
        <div>
          <div className="page-breadcrumb">
            <Link to="/dashboard">Dashboard</Link>
            <span>/</span>
            <span>Profile Settings</span>
          </div>
          <h1 className="page-title">Account Settings</h1>
          <p className="page-subtitle">Manage your personal profile and security credentials</p>
        </div>
      </div>

      {/* Profile Overview Card */}
      <div className="card" style={{ marginBottom: '20px', padding: '24px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '20px', flexWrap: 'wrap' }}>
          <Avatar name={profile?.name} src={profile?.avatarUrl} size="avatar-xl" />
          <div style={{ flex: 1 }}>
            <h2 style={{ fontSize: '18px', fontWeight: 700, color: 'var(--text-primary)' }}>{profile?.name}</h2>
            <div style={{ fontSize: '13px', color: 'var(--text-subtle)', marginTop: '2px' }}>{profile?.email}</div>
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginTop: '10px' }}>
              <span className="badge badge-active" style={{ fontSize: '10.5px' }}>{profile?.role || 'MEMBER'}</span>
              <span className="badge badge-completed" style={{ fontSize: '10.5px' }}>
                ● Active
              </span>
            </div>
            <div style={{ fontSize: '11.5px', color: 'var(--text-subtle)', marginTop: '8px' }}>
              Member since {formatDate(profile?.createdAt)}
            </div>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="tabs" style={{ marginBottom: '20px' }}>
        <button
          className={`tab ${activeTab === 'profile' ? 'active' : ''}`}
          onClick={() => setActiveTab('profile')}
        >
          <span style={{ display: 'inline-flex', alignItems: 'center', gap: '6px' }}>
            <User size={14} /> Profile Information
          </span>
        </button>
        <button
          className={`tab ${activeTab === 'security' ? 'active' : ''}`}
          onClick={() => setActiveTab('security')}
        >
          <span style={{ display: 'inline-flex', alignItems: 'center', gap: '6px' }}>
            <Lock size={14} /> Password & Security
          </span>
        </button>
      </div>

      {activeTab === 'profile' && (
        <div className="card">
          <div className="card-header">
            <h3 style={{ fontSize: '15px', fontWeight: 600 }}>Personal Details</h3>
          </div>
          <div className="card-body">
            <form onSubmit={handleProfileSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div className="form-group">
                <label className="form-label">Full Name <sup>*</sup></label>
                <input
                  type="text"
                  className="input"
                  value={form.name}
                  onChange={e => setForm(f => ({ ...f, name: e.target.value }))}
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label">Bio</label>
                <textarea
                  className="input"
                  placeholder="Share a short bio or your responsibilities on the team…"
                  value={form.bio}
                  onChange={e => setForm(f => ({ ...f, bio: e.target.value }))}
                  rows={3}
                />
              </div>

              <div className="form-group">
                <label className="form-label">Avatar Image URL</label>
                <input
                  type="url"
                  className="input"
                  placeholder="https://example.com/avatar.jpg"
                  value={form.avatarUrl}
                  onChange={e => setForm(f => ({ ...f, avatarUrl: e.target.value }))}
                />
                <span className="form-hint">Paste an image link to replace your initials avatar</span>
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', paddingTop: '8px' }}>
                <button type="submit" className="btn btn-primary" disabled={updateProfile.isPending}>
                  {updateProfile.isPending ? <div className="spinner spinner-sm" style={{ borderTopColor: 'white' }} /> : <Save size={14} />}
                  {updateProfile.isPending ? 'Saving…' : 'Save Changes'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {activeTab === 'security' && (
        <div className="card">
          <div className="card-header">
            <h3 style={{ fontSize: '15px', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '8px' }}>
              <Shield size={16} /> Update Password
            </h3>
          </div>
          <div className="card-body">
            <form onSubmit={handlePasswordSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
              <div className="form-group">
                <label className="form-label">Current Password <sup>*</sup></label>
                <input
                  type="password"
                  className="input"
                  value={pwForm.currentPassword}
                  onChange={e => setPwForm(f => ({ ...f, currentPassword: e.target.value }))}
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label">New Password <sup>*</sup></label>
                <input
                  type="password"
                  className="input"
                  placeholder="Minimum 8 characters"
                  value={pwForm.newPassword}
                  onChange={e => setPwForm(f => ({ ...f, newPassword: e.target.value }))}
                  required
                  minLength={8}
                />
              </div>

              <div className="form-group">
                <label className="form-label">Confirm New Password <sup>*</sup></label>
                <input
                  type="password"
                  className="input"
                  value={pwForm.confirmPassword}
                  onChange={e => setPwForm(f => ({ ...f, confirmPassword: e.target.value }))}
                  required
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-end', paddingTop: '8px' }}>
                <button type="submit" className="btn btn-primary" disabled={changePassword.isPending}>
                  {changePassword.isPending ? <div className="spinner spinner-sm" style={{ borderTopColor: 'white' }} /> : <Lock size={14} />}
                  {changePassword.isPending ? 'Updating…' : 'Change Password'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
