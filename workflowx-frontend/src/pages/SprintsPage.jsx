import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { sprintApi, projectApi, workspaceApi } from '../api'
import { LoadingPage } from '../components/Spinner'
import { StatusBadge } from '../components/Badge'
import Modal from '../components/Modal'
import { handleApiError, formatDate, SPRINT_STATUSES, timeAgo } from '../utils/helpers'
import toast from 'react-hot-toast'
import { Plus, Zap, Calendar, Play, CheckCircle, Trash2, Target } from 'lucide-react'
import { Link, useSearchParams } from 'react-router-dom'

function CreateSprintModal({ isOpen, onClose, projects, defaultProjectId }) {
  const qc = useQueryClient()
  const [form, setForm] = useState({
    name: '',
    goal: '',
    projectId: defaultProjectId || projects[0]?.id || '',
    startDate: '',
    endDate: ''
  })
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      await sprintApi.create({ ...form, projectId: Number(form.projectId) })
      qc.invalidateQueries({ queryKey: ['sprints'] })
      toast.success('Sprint created successfully')
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
      title="Create Sprint"
      footer={
        <>
          <button type="button" className="btn btn-default" onClick={onClose}>Cancel</button>
          <button type="submit" form="create-sprint-form" className="btn btn-primary" disabled={loading || !projects.length}>
            {loading ? <div className="spinner spinner-sm" style={{ borderTopColor: 'white' }} /> : <Plus size={15} />}
            {loading ? 'Creating…' : 'Create Sprint'}
          </button>
        </>
      }
    >
      <form id="create-sprint-form" onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
        <div className="form-group">
          <label className="form-label">Sprint Name <sup>*</sup></label>
          <input
            type="text"
            className="input"
            placeholder="e.g. Sprint 1 - Core Flow"
            value={form.name}
            onChange={e => setForm({ ...form, name: e.target.value })}
            required
            autoFocus
          />
        </div>
        <div className="form-group">
          <label className="form-label">Project <sup>*</sup></label>
          <select
            className="input"
            value={form.projectId}
            onChange={e => setForm({ ...form, projectId: e.target.value })}
            required
          >
            {projects.map(p => <option key={p.id} value={p.id}>{p.name}</option>)}
          </select>
        </div>
        <div className="form-group">
          <label className="form-label">Sprint Goal</label>
          <textarea
            className="input"
            placeholder="What is the objective or outcome of this sprint?"
            value={form.goal}
            onChange={e => setForm({ ...form, goal: e.target.value })}
            rows={2}
          />
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
          <div className="form-group">
            <label className="form-label">Start Date</label>
            <input
              type="date"
              className="input"
              value={form.startDate}
              onChange={e => setForm({ ...form, startDate: e.target.value })}
            />
          </div>
          <div className="form-group">
            <label className="form-label">End Date</label>
            <input
              type="date"
              className="input"
              value={form.endDate}
              onChange={e => setForm({ ...form, endDate: e.target.value })}
            />
          </div>
        </div>
      </form>
    </Modal>
  )
}

export default function SprintsPage() {
  const [showCreate, setShowCreate] = useState(false)
  const [searchParams] = useSearchParams()
  const projectId = searchParams.get('projectId')
  const qc = useQueryClient()

  const { data: wsData } = useQuery({ queryKey: ['workspaces'], queryFn: () => workspaceApi.getAll() })
  const workspaces = wsData?.data?.data || []

  const { data: projectsRes } = useQuery({
    queryKey: ['projects', workspaces[0]?.id],
    queryFn: () => projectApi.getByWorkspace(workspaces[0]?.id),
    enabled: !!workspaces[0]?.id,
  })
  const projects = projectsRes?.data?.data || []
  const activeProject = projectId ? projects.find(p => String(p.id) === projectId) : projects[0]

  const { data, isLoading } = useQuery({
    queryKey: ['sprints', activeProject?.id],
    queryFn: () => sprintApi.getByProject(activeProject?.id),
    enabled: !!activeProject?.id,
  })

  const updateStatus = useMutation({
    mutationFn: ({ id, status }) => sprintApi.updateStatus(id, status),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['sprints'] }); toast.success('Sprint status updated') },
    onError: (err) => toast.error(handleApiError(err)),
  })

  const deleteSprint = useMutation({
    mutationFn: (id) => sprintApi.delete(id),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['sprints'] }); toast.success('Sprint deleted') },
    onError: (err) => toast.error(handleApiError(err)),
  })

  const sprints = data?.data?.data || []
  if (isLoading) return <LoadingPage />

  return (
    <div>
      <div className="page-header">
        <div>
          <div className="page-breadcrumb">
            <Link to="/dashboard">Dashboard</Link>
            <span>/</span>
            <span>Sprints</span>
          </div>
          <h1 className="page-title">Sprints</h1>
          <p className="page-subtitle">Plan and execute iterations with your team</p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowCreate(true)} disabled={!projects.length}>
          <Plus size={16} /> New Sprint
        </button>
      </div>

      {/* Project selector pills */}
      {projects.length > 0 && (
        <div className="tabs-pill" style={{ marginBottom: '24px' }}>
          {projects.map(p => (
            <Link
              key={p.id}
              to={`/sprints?projectId=${p.id}`}
              className={`tab-pill ${String(p.id) === String(activeProject?.id) ? 'active' : ''}`}
            >
              {p.name}
            </Link>
          ))}
        </div>
      )}

      {sprints.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-icon"><Zap size={28} /></div>
          <h3 className="empty-state-title">No sprints configured</h3>
          <p className="empty-state-desc">
            {activeProject ? `Create a sprint for ${activeProject.name} to start time-boxed task cycles.` : 'Select or create a project first.'}
          </p>
          {projects.length > 0 && (
            <button className="btn btn-primary" onClick={() => setShowCreate(true)}>
              <Plus size={15} /> Create Sprint
            </button>
          )}
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
          {sprints.map(sprint => (
            <div
              key={sprint.id}
              className="card"
              style={{
                borderLeft: sprint.status === 'ACTIVE'
                  ? '4px solid var(--blue-500)'
                  : sprint.status === 'COMPLETED'
                  ? '4px solid var(--green-500)'
                  : '4px solid var(--gray-200)',
              }}
            >
              <div className="card-body" style={{ padding: '16px 20px' }}>
                <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: '16px', flexWrap: 'wrap' }}>
                  <div style={{ flex: 1, minWidth: '240px' }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '6px' }}>
                      <Zap size={16} style={{ color: sprint.status === 'ACTIVE' ? 'var(--blue-500)' : 'var(--gray-400)' }} />
                      <h3 style={{ fontSize: '15px', fontWeight: 600, color: 'var(--text-primary)' }}>{sprint.name}</h3>
                      <StatusBadge status={sprint.status} />
                    </div>

                    {sprint.goal && (
                      <div style={{ display: 'flex', alignItems: 'center', gap: '6px', fontSize: '13px', color: 'var(--text-secondary)', marginBottom: '8px' }}>
                        <Target size={13} style={{ color: 'var(--text-subtle)' }} />
                        <span>{sprint.goal}</span>
                      </div>
                    )}

                    <div style={{ display: 'flex', gap: '16px', fontSize: '12px', color: 'var(--text-subtle)' }}>
                      {sprint.startDate && (
                        <span style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
                          <Calendar size={12} />
                          {formatDate(sprint.startDate)} — {formatDate(sprint.endDate)}
                        </span>
                      )}
                      <span>Created {timeAgo(sprint.createdAt)}</span>
                    </div>
                  </div>

                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    {sprint.status === 'PLANNED' && (
                      <button
                        className="btn btn-default btn-sm"
                        onClick={() => updateStatus.mutate({ id: sprint.id, status: 'ACTIVE' })}
                        title="Start sprint"
                      >
                        <Play size={12} style={{ color: 'var(--blue-600)' }} /> Start Sprint
                      </button>
                    )}
                    {sprint.status === 'ACTIVE' && (
                      <button
                        className="btn btn-default btn-sm"
                        style={{ color: 'var(--green-600)', borderColor: 'var(--green-500)' }}
                        onClick={() => updateStatus.mutate({ id: sprint.id, status: 'COMPLETED' })}
                        title="Complete sprint"
                      >
                        <CheckCircle size={12} /> Complete Sprint
                      </button>
                    )}
                    <button
                      className="btn btn-subtle btn-icon-sm"
                      title="Delete sprint"
                      onClick={() => { if (confirm('Delete this sprint?')) deleteSprint.mutate(sprint.id) }}
                    >
                      <Trash2 size={13} style={{ color: 'var(--gray-400)' }} />
                    </button>
                  </div>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      <CreateSprintModal
        isOpen={showCreate}
        onClose={() => setShowCreate(false)}
        projects={projects}
        defaultProjectId={activeProject?.id}
      />
    </div>
  )
}
