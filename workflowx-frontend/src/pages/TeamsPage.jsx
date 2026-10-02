import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { teamApi, workspaceApi } from '../api'
import { LoadingPage } from '../components/Spinner'
import Modal from '../components/Modal'
import { handleApiError, timeAgo } from '../utils/helpers'
import toast from 'react-hot-toast'
import { Plus, Users, Trash2, UserPlus, Shield } from 'lucide-react'
import { Link, useSearchParams } from 'react-router-dom'

function CreateTeamModal({ isOpen, onClose, workspaces, activeWsId }) {
  const qc = useQueryClient()
  const [form, setForm] = useState({ name: '', description: '', workspaceId: activeWsId || workspaces[0]?.id || '' })
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      await teamApi.create({ ...form, workspaceId: Number(form.workspaceId) })
      qc.invalidateQueries({ queryKey: ['teams'] })
      toast.success('Team created successfully')
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
      title="Create New Team"
      footer={
        <>
          <button type="button" className="btn btn-default" onClick={onClose}>Cancel</button>
          <button type="submit" form="create-team-form" className="btn btn-primary" disabled={loading}>
            {loading ? <div className="spinner spinner-sm" style={{ borderTopColor: 'white' }} /> : <Plus size={15} />}
            {loading ? 'Creating…' : 'Create Team'}
          </button>
        </>
      }
    >
      <form id="create-team-form" onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
        <div className="form-group">
          <label className="form-label">Team Name <sup>*</sup></label>
          <input
            type="text"
            className="input"
            placeholder="e.g. Core Platform Engineers"
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
          >
            {workspaces.map(ws => <option key={ws.id} value={ws.id}>{ws.name}</option>)}
          </select>
        </div>
        <div className="form-group">
          <label className="form-label">Description</label>
          <textarea
            className="input"
            placeholder="What does this team focus on?"
            value={form.description}
            onChange={e => setForm({ ...form, description: e.target.value })}
            rows={3}
          />
        </div>
      </form>
    </Modal>
  )
}

export default function TeamsPage() {
  const [showCreate, setShowCreate] = useState(false)
  const [searchParams] = useSearchParams()
  const workspaceId = searchParams.get('workspaceId')
  const qc = useQueryClient()

  const { data: wsData } = useQuery({ queryKey: ['workspaces'], queryFn: () => workspaceApi.getAll() })
  const workspaces = wsData?.data?.data || []
  const activeWsId = workspaceId || workspaces[0]?.id

  const { data, isLoading } = useQuery({
    queryKey: ['teams', activeWsId],
    queryFn: () => teamApi.getByWorkspace(activeWsId),
    enabled: !!activeWsId,
  })

  const deleteTeam = useMutation({
    mutationFn: (id) => teamApi.delete(id),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['teams'] }); toast.success('Team deleted') },
    onError: (err) => toast.error(handleApiError(err)),
  })

  const teams = data?.data?.data || []
  if (isLoading) return <LoadingPage />

  return (
    <div>
      <div className="page-header">
        <div>
          <div className="page-breadcrumb">
            <Link to="/dashboard">Dashboard</Link>
            <span>/</span>
            <span>People & Teams</span>
          </div>
          <h1 className="page-title">Teams</h1>
          <p className="page-subtitle">Collaborate across departments and feature squads</p>
        </div>
        <button className="btn btn-primary" onClick={() => setShowCreate(true)} disabled={!workspaces.length}>
          <Plus size={16} /> New Team
        </button>
      </div>

      {workspaces.length > 0 && (
        <div className="tabs-pill" style={{ marginBottom: '24px' }}>
          {workspaces.map(ws => (
            <Link
              key={ws.id}
              to={`/teams?workspaceId=${ws.id}`}
              className={`tab-pill ${String(ws.id) === String(activeWsId) ? 'active' : ''}`}
            >
              {ws.name}
            </Link>
          ))}
        </div>
      )}

      {teams.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-icon"><Users size={28} /></div>
          <h3 className="empty-state-title">No teams yet</h3>
          <p className="empty-state-desc">Create teams to organize members and route project responsibilities.</p>
          <button className="btn btn-primary" onClick={() => setShowCreate(true)}>
            <Plus size={15} /> Create Team
          </button>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(300px, 1fr))', gap: '16px' }}>
          {teams.map(team => (
            <div key={team.id} className="card" style={{ display: 'flex', flexDirection: 'column' }}>
              <div className="card-body" style={{ flex: 1 }}>
                <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', marginBottom: '12px' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <div style={{
                      width: '40px',
                      height: '40px',
                      borderRadius: 'var(--r-lg)',
                      background: 'var(--blue-50)',
                      border: '1px solid var(--blue-100)',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      color: 'var(--blue-600)'
                    }}>
                      <Users size={20} />
                    </div>
                    <div>
                      <h3 style={{ fontSize: '15px', fontWeight: 600, color: 'var(--text-primary)' }}>{team.name}</h3>
                      <div style={{ fontSize: '12px', color: 'var(--text-subtle)', display: 'flex', alignItems: 'center', gap: '4px', marginTop: '2px' }}>
                        <span className="badge badge-planning" style={{ padding: '1px 6px', fontSize: '10px' }}>
                          {team.memberCount || 0} members
                        </span>
                      </div>
                    </div>
                  </div>

                  <button
                    className="btn btn-subtle btn-icon-sm"
                    title="Delete team"
                    onClick={() => { if (confirm(`Delete team "${team.name}"?`)) deleteTeam.mutate(team.id) }}
                  >
                    <Trash2 size={13} style={{ color: 'var(--gray-400)' }} />
                  </button>
                </div>

                <p style={{
                  fontSize: '13px',
                  color: 'var(--text-secondary)',
                  lineHeight: 1.5,
                  marginBottom: '14px',
                  minHeight: '38px',
                  WebkitLineClamp: 2,
                  overflow: 'hidden',
                  display: '-webkit-box',
                  WebkitBoxOrient: 'vertical'
                }}>
                  {team.description || 'No description provided for this team.'}
                </p>

                <div style={{ fontSize: '12px', color: 'var(--text-subtle)', paddingTop: '10px', borderTop: '1px solid var(--border-light)' }}>
                  Created {timeAgo(team.createdAt)}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {showCreate && (
        <CreateTeamModal
          isOpen={showCreate}
          onClose={() => setShowCreate(false)}
          workspaces={workspaces}
          activeWsId={activeWsId}
        />
      )}
    </div>
  )
}
