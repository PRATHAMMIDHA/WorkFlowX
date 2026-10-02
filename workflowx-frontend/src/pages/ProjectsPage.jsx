import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { projectApi, workspaceApi } from '../api'
import { LoadingPage } from '../components/Spinner'
import { StatusBadge } from '../components/Badge'
import Modal from '../components/Modal'
import { handleApiError, formatDate, PROJECT_STATUSES, timeAgo } from '../utils/helpers'
import toast from 'react-hot-toast'
import { Plus, FolderKanban, Calendar, Trash2, ExternalLink, ArrowRight, Layers } from 'lucide-react'
import { Link, useSearchParams } from 'react-router-dom'

function CreateProjectModal({ isOpen, onClose, workspaces }) {
  const qc = useQueryClient()
  const [form, setForm] = useState({ name: '', description: '', workspaceId: workspaces[0]?.id || '', status: 'PLANNING', startDate: '', endDate: '' })
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      await projectApi.create({ ...form, workspaceId: Number(form.workspaceId) })
      qc.invalidateQueries({ queryKey: ['projects'] })
      toast.success('Project created successfully')
      onClose()
    } catch (err) {
      toast.error(handleApiError(err))
    } finally {
      setLoading(false)
    }
  }

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Create Project"
      footer={
        <>
          <button type="button" className="btn btn-default" onClick={onClose}>Cancel</button>
          <button type="submit" form="create-project-form" className="btn btn-primary" disabled={loading || !workspaces.length}>
            {loading ? <div className="spinner spinner-sm" style={{ borderTopColor: 'white' }} /> : <Plus size={15} />}
            {loading ? 'Creating…' : 'Create Project'}
          </button>
        </>
      }
    >
      <form id="create-project-form" onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
        <div className="form-group">
          <label className="form-label">Project Name <sup>*</sup></label>
          <input
            type="text"
            className="input"
            placeholder="e.g. Mobile App Redesign"
            value={form.name}
            onChange={e => setForm({ ...form, name: e.target.value })}
            required
            autoFocus
          />
        </div>
        <div className="form-group">
          <label className="form-label">Workspace <sup>*</sup></label>
          <select
            className="input"
            value={form.workspaceId}
            onChange={e => setForm({ ...form, workspaceId: e.target.value })}
            required
          >
            {workspaces.map(ws => <option key={ws.id} value={ws.id}>{ws.name}</option>)}
          </select>
        </div>
        <div className="form-group">
          <label className="form-label">Description</label>
          <textarea
            className="input"
            placeholder="What is this project about?"
            value={form.description}
            onChange={e => setForm({ ...form, description: e.target.value })}
            rows={3}
          />
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
          <div className="form-group">
            <label className="form-label">Status</label>
            <select className="input" value={form.status} onChange={e => setForm({ ...form, status: e.target.value })}>
              {PROJECT_STATUSES.map(s => <option key={s} value={s}>{s}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Start Date</label>
            <input
              type="date"
              className="input"
              value={form.startDate}
              onChange={e => setForm({ ...form, startDate: e.target.value })}
            />
          </div>
        </div>
      </form>
    </Modal>
  )
}

export default function ProjectsPage() {
  const [showCreate, setShowCreate] = useState(false)
  const [searchQuery, setSearchQuery] = useState('')
  const [searchParams] = useSearchParams()
  const workspaceId = searchParams.get('workspaceId')
  const qc = useQueryClient()

  const { data: wsData } = useQuery({ queryKey: ['workspaces'], queryFn: () => workspaceApi.getAll() })
  const workspaces = wsData?.data?.data || []

  const activeWsId = workspaceId || workspaces[0]?.id

  const { data, isLoading } = useQuery({
    queryKey: ['projects', activeWsId],
    queryFn: () => projectApi.getByWorkspace(activeWsId),
    enabled: !!activeWsId,
  })

  const deleteMut = useMutation({
    mutationFn: (id) => projectApi.delete(id),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['projects'] }); toast.success('Project deleted') },
    onError: (err) => toast.error(handleApiError(err)),
  })

  const rawProjects = data?.data?.data || []
  const projects = rawProjects.filter(p => p.name.toLowerCase().includes(searchQuery.toLowerCase()))

  if (isLoading) return <LoadingPage />

  return (
    <div>
      <div className="page-header">
        <div>
          <div className="page-breadcrumb">
            <Link to="/dashboard">Dashboard</Link>
            <span>/</span>
            <span>Projects</span>
          </div>
          <h1 className="page-title">Projects</h1>
          <p className="page-subtitle">Track and coordinate work across all projects</p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowCreate(true)} disabled={!workspaces.length}>
          <Plus size={16} /> New Project
        </button>
      </div>

      {/* Workspace Tabs & Filter Bar */}
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '16px', flexWrap: 'wrap', marginBottom: '20px' }}>
        {workspaces.length > 0 && (
          <div className="tabs-pill" style={{ marginBottom: 0 }}>
            {workspaces.map(ws => (
              <Link
                key={ws.id}
                to={`/projects?workspaceId=${ws.id}`}
                className={`tab-pill ${String(ws.id) === String(activeWsId) ? 'active' : ''}`}
              >
                {ws.name}
              </Link>
            ))}
          </div>
        )}

        <div className="search-wrapper" style={{ marginLeft: workspaces.length ? 'auto' : 0 }}>
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
          <input
            type="text"
            className="search-input"
            placeholder="Search projects…"
            value={searchQuery}
            onChange={e => setSearchQuery(e.target.value)}
          />
        </div>
      </div>

      {projects.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-icon"><FolderKanban size={28} /></div>
          <h3 className="empty-state-title">No projects found</h3>
          <p className="empty-state-desc">
            {searchQuery ? `No projects matching "${searchQuery}"` : 'Create your first project to start organizing tasks and sprints'}
          </p>
          {!searchQuery && (
            <button className="btn btn-primary" onClick={() => setShowCreate(true)} disabled={!workspaces.length}>
              <Plus size={15} /> Create Project
            </button>
          )}
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: '16px' }}>
          {projects.map(project => (
            <div
              key={project.id}
              className="card"
              style={{
                display: 'flex',
                flexDirection: 'column',
                transition: 'box-shadow var(--t-fast), border-color var(--t-fast)',
                borderTop: '3px solid var(--blue-500)',
              }}
            >
              <div className="card-body" style={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
                <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: '12px', marginBottom: '8px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                    <div style={{
                      width: '32px',
                      height: '32px',
                      borderRadius: 'var(--r-md)',
                      background: 'var(--blue-50)',
                      color: 'var(--blue-600)',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      fontWeight: 700,
                      fontSize: '13px'
                    }}>
                      {project.name.charAt(0).toUpperCase()}
                    </div>
                    <div>
                      <h3 style={{ fontSize: '15px', fontWeight: 600, color: 'var(--text-primary)' }}>
                        <Link to={`/projects/${project.id}`} style={{ color: 'inherit' }}>
                          {project.name}
                        </Link>
                      </h3>
                      <div style={{ fontSize: '11px', color: 'var(--text-subtle)', textTransform: 'uppercase', letterSpacing: '0.4px', fontWeight: 600 }}>
                        KEY-{project.id}
                      </div>
                    </div>
                  </div>

                  <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                    <StatusBadge status={project.status} />
                    <button
                      className="btn btn-subtle btn-icon-sm"
                      title="Delete project"
                      onClick={() => { if (confirm(`Are you sure you want to delete "${project.name}"?`)) deleteMut.mutate(project.id) }}
                    >
                      <Trash2 size={13} style={{ color: 'var(--gray-400)' }} />
                    </button>
                  </div>
                </div>

                <p style={{
                  fontSize: '13px',
                  color: 'var(--text-secondary)',
                  lineHeight: 1.5,
                  WebkitLineClamp: 2,
                  overflow: 'hidden',
                  display: '-webkit-box',
                  WebkitBoxOrient: 'vertical',
                  marginBottom: '16px',
                  minHeight: '38px',
                }}>
                  {project.description || 'No description provided.'}
                </p>

                <div style={{ marginTop: 'auto', display: 'flex', alignItems: 'center', justifyContent: 'space-between', fontSize: '12px', color: 'var(--text-subtle)', paddingTop: '12px', borderTop: '1px solid var(--border-light)' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
                    <Calendar size={13} />
                    <span>{project.startDate ? formatDate(project.startDate) : 'No date'}</span>
                  </div>
                  <div>
                    {timeAgo(project.createdAt)}
                  </div>
                </div>
              </div>

              <div className="card-footer" style={{ padding: '8px 16px', background: 'var(--gray-10)', display: 'flex', justifyContent: 'flex-end' }}>
                <Link to={`/projects/${project.id}`} className="btn btn-default btn-sm" style={{ gap: '4px' }}>
                  View Board <ArrowRight size={13} />
                </Link>
              </div>
            </div>
          ))}
        </div>
      )}

      <CreateProjectModal isOpen={showCreate} onClose={() => setShowCreate(false)} workspaces={workspaces} />
    </div>
  )
}
