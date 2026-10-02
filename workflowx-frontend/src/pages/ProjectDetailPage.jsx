import { useState } from 'react'
import { useParams } from 'react-router-dom'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { taskApi, projectApi, sprintApi } from '../api'
import { LoadingPage } from '../components/Spinner'
import { StatusBadge, PriorityBadge } from '../components/Badge'
import Modal from '../components/Modal'
import Avatar from '../components/Avatar'
import { handleApiError, KANBAN_COLUMNS, TASK_STATUSES, TASK_PRIORITIES, formatDate } from '../utils/helpers'
import toast from 'react-hot-toast'
import { Plus, Kanban, List, Clock, User, ChevronRight } from 'lucide-react'
import { Link } from 'react-router-dom'

const COL_COLORS = { BACKLOG: '#5E6C84', TODO: '#172B4D', IN_PROGRESS: '#0052CC', CODE_REVIEW: '#403294', DONE: '#006644' }

function CreateTaskModal({ isOpen, onClose, projectId, sprints, members }) {
  const qc = useQueryClient()
  const [form, setForm] = useState({ title: '', description: '', projectId: Number(projectId), status: 'BACKLOG', priority: 'MEDIUM', assigneeId: '', sprintId: '', dueDate: '' })
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      const payload = { ...form, assigneeId: form.assigneeId ? Number(form.assigneeId) : null, sprintId: form.sprintId ? Number(form.sprintId) : null, dueDate: form.dueDate || null }
      await taskApi.create(payload)
      qc.invalidateQueries({ queryKey: ['tasks', projectId] })
      toast.success('Task created')
      onClose()
    } catch (err) { toast.error(handleApiError(err)) }
    finally { setLoading(false) }
  }

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Create task" size="modal-lg"
      footer={
        <>
          <button className="btn btn-default" onClick={onClose}>Cancel</button>
          <button className="btn btn-primary" form="create-task-form" type="submit" disabled={loading}>
            {loading ? <div className="spinner spinner-sm" style={{ borderTopColor: 'white' }} /> : null}
            {loading ? 'Creating…' : 'Create task'}
          </button>
        </>
      }>
      <form id="create-task-form" onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
        <div className="form-group">
          <label className="form-label">Task title <sup>*</sup></label>
          <input className="input" placeholder="What needs to be done?" value={form.title} onChange={e => setForm({ ...form, title: e.target.value })} required />
        </div>
        <div className="form-group">
          <label className="form-label">Description</label>
          <textarea className="input" placeholder="Add more details…" value={form.description} onChange={e => setForm({ ...form, description: e.target.value })} rows={4} />
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
          <div className="form-group">
            <label className="form-label">Status</label>
            <select className="input" value={form.status} onChange={e => setForm({ ...form, status: e.target.value })}>
              {TASK_STATUSES.map(s => <option key={s} value={s}>{s.replace(/_/g, ' ')}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Priority</label>
            <select className="input" value={form.priority} onChange={e => setForm({ ...form, priority: e.target.value })}>
              {TASK_PRIORITIES.map(p => <option key={p} value={p}>{p}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Assignee</label>
            <select className="input" value={form.assigneeId} onChange={e => setForm({ ...form, assigneeId: e.target.value })}>
              <option value="">Unassigned</option>
              {members.map(m => <option key={m.userId} value={m.userId}>{m.userName}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Sprint</label>
            <select className="input" value={form.sprintId} onChange={e => setForm({ ...form, sprintId: e.target.value })}>
              <option value="">No sprint</option>
              {sprints.map(s => <option key={s.id} value={s.id}>{s.name}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label className="form-label">Due date</label>
            <input type="date" className="input" value={form.dueDate} onChange={e => setForm({ ...form, dueDate: e.target.value })} />
          </div>
        </div>
      </form>
    </Modal>
  )
}

function KanbanBoard({ tasks, projectId }) {
  const qc = useQueryClient()
  const updateStatus = useMutation({
    mutationFn: ({ taskId, status }) => taskApi.updateStatus(taskId, status),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['tasks', projectId] }),
    onError: (err) => toast.error(handleApiError(err)),
  })
  const columnTasks = (colId) => tasks.filter(t => t.status === colId)

  return (
    <div className="kanban-board">
      {KANBAN_COLUMNS.map(col => {
        const colTasks = columnTasks(col.id)
        return (
          <div key={col.id} className="kanban-column">
            <div className="kanban-column-header">
              <div className="kanban-column-title">
                <span style={{ width: '10px', height: '10px', borderRadius: '50%', background: COL_COLORS[col.id], flexShrink: 0, display: 'inline-block' }} />
                {col.label}
              </div>
              <span className="kanban-count">{colTasks.length}</span>
            </div>
            <div className="kanban-cards">
              {colTasks.map(task => (
                <div key={task.id} className="task-card">
                  <div className="task-card-title">{task.title}</div>
                  <div className="task-card-meta">
                    <PriorityBadge priority={task.priority} />
                    {task.dueDate && (
                      <span style={{ fontSize: '11px', color: new Date(task.dueDate) < new Date() ? 'var(--red-500)' : 'var(--text-subtle)', display: 'flex', alignItems: 'center', gap: '3px' }}>
                        <Clock size={10} />{formatDate(task.dueDate)}
                      </span>
                    )}
                  </div>
                  <div className="task-card-footer">
                    {task.assigneeName
                      ? <div style={{ display: 'flex', alignItems: 'center', gap: '5px', fontSize: '11.5px', color: 'var(--text-subtle)' }}><Avatar name={task.assigneeName} size="avatar-sm" />{task.assigneeName.split(' ')[0]}</div>
                      : <span style={{ fontSize: '11.5px', color: 'var(--text-subtle)', display: 'flex', alignItems: 'center', gap: '3px' }}><User size={11} />Unassigned</span>
                    }
                    <select value={task.status} onChange={e => updateStatus.mutate({ taskId: task.id, status: e.target.value })}
                      style={{ fontSize: '11px', border: '1px solid var(--border-light)', borderRadius: 'var(--r-sm)', padding: '2px 4px', color: 'var(--text-subtle)', background: 'var(--bg-surface)', cursor: 'pointer' }}
                      onClick={e => e.stopPropagation()}>
                      {TASK_STATUSES.map(s => <option key={s} value={s}>{s.replace(/_/g,' ')}</option>)}
                    </select>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )
      })}
    </div>
  )
}

export default function ProjectDetailPage() {
  const { projectId } = useParams()
  const [view, setView] = useState('kanban')
  const [showCreate, setShowCreate] = useState(false)

  const { data: projectRes } = useQuery({ queryKey: ['project', projectId], queryFn: () => projectApi.getById(projectId) })
  const { data: tasksRes, isLoading } = useQuery({ queryKey: ['tasks', projectId], queryFn: () => taskApi.getAll({ projectId: Number(projectId), size: 200 }) })
  const { data: sprintsRes } = useQuery({ queryKey: ['sprints', projectId], queryFn: () => sprintApi.getByProject(projectId) })
  const { data: membersRes } = useQuery({ queryKey: ['projectMembers', projectId], queryFn: () => projectApi.getMembers(projectId) })
  const { data: statsRes } = useQuery({ queryKey: ['projectStats', projectId], queryFn: () => projectApi.getStats(projectId) })

  const project = projectRes?.data?.data
  const tasks = tasksRes?.data?.data?.content || []
  const sprints = sprintsRes?.data?.data || []
  const members = membersRes?.data?.data || []
  const stats = statsRes?.data?.data

  if (isLoading) return <LoadingPage />

  return (
    <div>
      {/* Breadcrumb */}
      <div className="page-breadcrumb">
        <Link to="/projects">Projects</Link>
        <ChevronRight size={12} />
        <span style={{ color: 'var(--text-primary)', fontWeight: 500 }}>{project?.name || 'Project'}</span>
      </div>

      <div className="page-header" style={{ marginTop: '4px' }}>
        <div>
          <h1 className="page-title">{project?.name || 'Project'}</h1>
          <p className="page-subtitle">{project?.workspaceName} · {tasks.length} tasks</p>
        </div>
        <div style={{ display: 'flex', gap: '8px', alignItems: 'center' }}>
          {/* View toggle */}
          <div className="tabs-pill">
            <button className={`tab-pill ${view === 'kanban' ? 'active' : ''}`} onClick={() => setView('kanban')} style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
              <Kanban size={13} /> Board
            </button>
            <button className={`tab-pill ${view === 'list' ? 'active' : ''}`} onClick={() => setView('list')} style={{ display: 'flex', alignItems: 'center', gap: '5px' }}>
              <List size={13} /> List
            </button>
          </div>
          <button className="btn btn-primary" onClick={() => setShowCreate(true)}>
            <Plus size={14} /> Create task
          </button>
        </div>
      </div>

      {/* Stats row */}
      {stats && (
        <div style={{ display: 'flex', gap: '12px', marginBottom: '20px', flexWrap: 'wrap' }}>
          {[
            { label: 'Total', value: stats.totalTasks, color: 'var(--blue-600)' },
            { label: 'Done', value: stats.completedTasks, color: 'var(--green-500)' },
            { label: 'Active', value: stats.activeTasks, color: 'var(--yellow-500)' },
            { label: 'Backlog', value: stats.backlogTasks, color: 'var(--gray-400)' },
            { label: 'Overdue', value: stats.overdueTasks, color: 'var(--red-500)' },
          ].map(s => (
            <div key={s.label} style={{ background: 'var(--bg-surface)', border: '1px solid var(--border-light)', borderRadius: 'var(--r-lg)', padding: '10px 20px', textAlign: 'center', minWidth: '100px', boxShadow: 'var(--shadow-xs)' }}>
              <div style={{ fontSize: '22px', fontWeight: 700, color: s.color, fontFamily: 'var(--font-display)', lineHeight: 1 }}>{s.value}</div>
              <div style={{ fontSize: '11px', color: 'var(--text-subtle)', marginTop: '3px', fontWeight: 500, textTransform: 'uppercase', letterSpacing: '0.5px' }}>{s.label}</div>
            </div>
          ))}
        </div>
      )}

      {view === 'kanban'
        ? <KanbanBoard tasks={tasks} projectId={projectId} />
        : (
          <div className="table-wrapper">
            <table className="table">
              <thead><tr><th>Title</th><th>Status</th><th>Priority</th><th>Assignee</th><th>Sprint</th><th>Due</th></tr></thead>
              <tbody>
                {tasks.length === 0
                  ? <tr><td colSpan={6} style={{ textAlign: 'center', padding: '40px', color: 'var(--text-subtle)' }}>No tasks yet</td></tr>
                  : tasks.map(task => (
                    <tr key={task.id}>
                      <td style={{ fontWeight: 500 }}>{task.title}</td>
                      <td><StatusBadge status={task.status} /></td>
                      <td><PriorityBadge priority={task.priority} /></td>
                      <td>
                        {task.assigneeName
                          ? <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}><Avatar name={task.assigneeName} size="avatar-sm" />{task.assigneeName}</div>
                          : <span style={{ color: 'var(--text-subtle)' }}>—</span>}
                      </td>
                      <td style={{ color: 'var(--text-subtle)', fontSize: '13px' }}>{task.sprintName || '—'}</td>
                      <td style={{ fontSize: '13px', color: task.dueDate && new Date(task.dueDate) < new Date() ? 'var(--red-500)' : 'var(--text-subtle)' }}>
                        {task.dueDate ? formatDate(task.dueDate) : '—'}
                      </td>
                    </tr>
                  ))}
              </tbody>
            </table>
          </div>
        )}

      <CreateTaskModal isOpen={showCreate} onClose={() => setShowCreate(false)} projectId={projectId} sprints={sprints} members={members} />
    </div>
  )
}
