import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { workspaceApi } from '../api'
import { LoadingPage } from '../components/Spinner'
import Modal from '../components/Modal'
import { handleApiError, timeAgo } from '../utils/helpers'
import toast from 'react-hot-toast'
import { Plus, FolderKanban, Users, Trash2, Settings, ArrowRight, Grid } from 'lucide-react'

function CreateWorkspaceModal({ isOpen, onClose }) {
  const qc = useQueryClient()
  const [form, setForm] = useState({ name: '', description: '' })
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      await workspaceApi.create(form)
      qc.invalidateQueries({ queryKey: ['workspaces'] })
      toast.success('Workspace created')
      onClose()
      setForm({ name: '', description: '' })
    } catch (err) { toast.error(handleApiError(err)) }
    finally { setLoading(false) }
  }

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Create workspace"
      footer={
        <>
          <button className="btn btn-default" onClick={onClose}>Cancel</button>
          <button className="btn btn-primary" form="create-ws-form" type="submit" disabled={loading}>
            {loading ? <div className="spinner spinner-sm" style={{ borderTopColor: 'white' }} /> : null}
            {loading ? 'Creating…' : 'Create workspace'}
          </button>
        </>
      }>
      <form id="create-ws-form" onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
        <div className="form-group">
          <label className="form-label">Workspace name <sup>*</sup></label>
          <input className="input" placeholder="e.g. Engineering, Product" value={form.name} onChange={e => setForm({ ...form, name: e.target.value })} required />
        </div>
        <div className="form-group">
          <label className="form-label">Description</label>
          <textarea className="input" placeholder="Describe what this workspace is for…" value={form.description} onChange={e => setForm({ ...form, description: e.target.value })} rows={3} />
        </div>
      </form>
    </Modal>
  )
}

export default function WorkspacesPage() {
  const [showCreate, setShowCreate] = useState(false)
  const qc = useQueryClient()

  const { data, isLoading } = useQuery({ queryKey: ['workspaces'], queryFn: () => workspaceApi.getAll() })
  const deleteMut = useMutation({
    mutationFn: (id) => workspaceApi.delete(id),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['workspaces'] }); toast.success('Workspace deleted') },
    onError: (err) => toast.error(handleApiError(err)),
  })

  const workspaces = data?.data?.data || []
  if (isLoading) return <LoadingPage />

  return (
    <div>
      <div className="page-header">
        <div>
          <h1 className="page-title">Workspaces</h1>
          <p className="page-subtitle">{workspaces.length} workspace{workspaces.length !== 1 ? 's' : ''}</p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowCreate(true)}>
          <Plus size={14} /> Create workspace
        </button>
      </div>

      {workspaces.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-icon"><Grid size={24} /></div>
          <h3 className="empty-state-title">No workspaces yet</h3>
          <p className="empty-state-desc">Create a workspace to organize your projects and invite your team</p>
          <button className="btn btn-primary" onClick={() => setShowCreate(true)}><Plus size={14} /> Create workspace</button>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '16px' }}>
          {workspaces.map(ws => (
            <div key={ws.id} className="card" style={{ display: 'flex', flexDirection: 'column' }}>
              {/* Card header strip */}
              <div style={{ height: '4px', background: 'var(--blue-500)', borderRadius: 'var(--r-lg) var(--r-lg) 0 0' }} />

              <div style={{ padding: '20px', flex: 1 }}>
                {/* Header row */}
                <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', marginBottom: '12px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                    <div style={{ width: '40px', height: '40px', borderRadius: 'var(--r-md)', background: 'var(--blue-50)', display: 'flex', alignItems: 'center', justifyContent: 'center', border: '1px solid var(--blue-100)' }}>
                      <FolderKanban size={18} color="var(--blue-600)" />
                    </div>
                    <div>
                      <div style={{ fontSize: '15px', fontWeight: 600, color: 'var(--text-primary)' }}>{ws.name}</div>
                      <div style={{ fontSize: '12px', color: 'var(--text-subtle)' }}>by {ws.ownerName}</div>
                    </div>
                  </div>
                  <div style={{ display: 'flex', gap: '2px' }}>
                    <Link to={`/workspaces/${ws.id}`} className="btn btn-subtle btn-icon" title="Manage">
                      <Settings size={14} />
                    </Link>
                    <button className="btn btn-subtle btn-icon" style={{ color: 'var(--red-500)' }}
                      onClick={() => { if (confirm(`Delete "${ws.name}"?`)) deleteMut.mutate(ws.id) }}>
                      <Trash2 size={14} />
                    </button>
                  </div>
                </div>

                {ws.description && (
                  <p style={{ fontSize: '13px', color: 'var(--text-subtle)', lineHeight: 1.5, marginBottom: '12px', display: '-webkit-box', WebkitLineClamp: 2, WebkitBoxOrient: 'vertical', overflow: 'hidden' }}>{ws.description}</p>
                )}

                <div style={{ display: 'flex', alignItems: 'center', gap: '12px', fontSize: '12px', color: 'var(--text-subtle)', paddingTop: '12px', borderTop: '1px solid var(--border-light)' }}>
                  <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                    <Users size={12} /> {ws.memberCount} member{ws.memberCount !== 1 ? 's' : ''}
                  </span>
                  <span style={{ marginLeft: 'auto' }}>{timeAgo(ws.createdAt)}</span>
                </div>
              </div>

              <div style={{ padding: '12px 20px', borderTop: '1px solid var(--border-light)' }}>
                <Link to={`/workspaces/${ws.id}/projects`} className="btn btn-default w-full" style={{ justifyContent: 'center', fontSize: '13px' }}>
                  View projects <ArrowRight size={13} />
                </Link>
              </div>
            </div>
          ))}
        </div>
      )}

      <CreateWorkspaceModal isOpen={showCreate} onClose={() => setShowCreate(false)} />
    </div>
  )
}
