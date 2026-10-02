import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { workspaceApi, taskApi } from '../api'
import useAuthStore from '../store/authStore'
import { StatusBadge } from '../components/Badge'
import { LoadingPage } from '../components/Spinner'
import Avatar from '../components/Avatar'
import { formatDate, timeAgo } from '../utils/helpers'
import { FolderKanban, CheckSquare, Briefcase, TrendingUp, Clock, AlertTriangle, Plus, ArrowRight } from 'lucide-react'

export default function DashboardPage() {
  const user = useAuthStore(s => s.user)
  const hour = new Date().getHours()
  const greeting = hour < 12 ? 'Good morning' : hour < 17 ? 'Good afternoon' : 'Good evening'

  const { data: workspacesRes, isLoading: wLoading } = useQuery({ queryKey: ['workspaces'], queryFn: () => workspaceApi.getAll() })
  const { data: myTasksRes, isLoading: tLoading } = useQuery({ queryKey: ['myTasks'], queryFn: () => taskApi.getAll({ assigneeId: user?.id, size: 20 }) })

  const workspaces = workspacesRes?.data?.data || []
  const tasks = myTasksRes?.data?.data?.content || []

  if (wLoading || tLoading) return <LoadingPage />

  const doneTasks = tasks.filter(t => t.status === 'DONE').length
  const inProgress = tasks.filter(t => t.status === 'IN_PROGRESS').length
  const overdue = tasks.filter(t => t.dueDate && new Date(t.dueDate) < new Date() && t.status !== 'DONE').length

  return (
    <div>
      {/* Header */}
      <div className="page-header">
        <div>
          <div style={{ fontSize: '13px', color: 'var(--text-subtle)', marginBottom: '4px' }}>
            {new Date().toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric' })}
          </div>
          <h1 className="page-title">{greeting}, {user?.name?.split(' ')[0]} 👋</h1>
        </div>
        <Link to="/tasks" className="btn btn-primary">
          <Plus size={14} /> New task
        </Link>
      </div>

      {/* Stats */}
      <div className="stats-grid">
        <div className="stat-card">
          <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between' }}>
            <div>
              <div className="stat-label">Total tasks</div>
              <div className="stat-value">{tasks.length}</div>
              <div className="stat-change">{inProgress} in progress</div>
            </div>
            <div style={{ padding: '8px', background: 'var(--blue-50)', borderRadius: 'var(--r-lg)' }}>
              <CheckSquare size={18} color="var(--blue-500)" />
            </div>
          </div>
        </div>
        <div className="stat-card">
          <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between' }}>
            <div>
              <div className="stat-label">Completed</div>
              <div className="stat-value">{doneTasks}</div>
              <div className={`stat-change ${tasks.length > 0 ? 'up' : ''}`}>
                {tasks.length > 0 ? `${Math.round(doneTasks/tasks.length*100)}% completion` : 'No tasks yet'}
              </div>
            </div>
            <div style={{ padding: '8px', background: 'var(--green-100)', borderRadius: 'var(--r-lg)' }}>
              <TrendingUp size={18} color="var(--green-500)" />
            </div>
          </div>
        </div>
        <div className="stat-card">
          <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between' }}>
            <div>
              <div className="stat-label">Workspaces</div>
              <div className="stat-value">{workspaces.length}</div>
              <div className="stat-change">{workspaces.length > 0 ? 'Active' : 'Create one'}</div>
            </div>
            <div style={{ padding: '8px', background: 'var(--purple-100)', borderRadius: 'var(--r-lg)' }}>
              <Briefcase size={18} color="var(--purple-500)" />
            </div>
          </div>
        </div>
        <div className="stat-card">
          <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between' }}>
            <div>
              <div className="stat-label">Overdue</div>
              <div className="stat-value" style={{ color: overdue > 0 ? 'var(--red-500)' : 'var(--text-primary)' }}>{overdue}</div>
              <div className={`stat-change ${overdue > 0 ? 'down' : ''}`}>
                {overdue > 0 ? 'Need attention' : 'All on schedule ✓'}
              </div>
            </div>
            <div style={{ padding: '8px', background: overdue > 0 ? 'var(--red-100)' : 'var(--green-100)', borderRadius: 'var(--r-lg)' }}>
              <AlertTriangle size={18} color={overdue > 0 ? 'var(--red-500)' : 'var(--green-500)'} />
            </div>
          </div>
        </div>
      </div>

      {/* Two column grid */}
      <div style={{ display: 'grid', gridTemplateColumns: '1fr 380px', gap: '20px' }}>
        {/* My Tasks */}
        <div className="card">
          <div className="card-header">
            <div>
              <h3 style={{ fontSize: '15px', fontWeight: 600 }}>My tasks</h3>
              <div style={{ fontSize: '12px', color: 'var(--text-subtle)', marginTop: '2px' }}>{tasks.length} assigned to you</div>
            </div>
            <Link to="/tasks" className="btn btn-subtle btn-sm" style={{ color: 'var(--text-link)', fontSize: '12px', display: 'flex', alignItems: 'center', gap: '4px' }}>
              View all <ArrowRight size={12} />
            </Link>
          </div>
          {tasks.length === 0 ? (
            <div className="empty-state" style={{ padding: '40px 20px' }}>
              <div className="empty-state-icon"><CheckSquare size={22} /></div>
              <p className="empty-state-title" style={{ fontSize: '14px' }}>No tasks assigned to you</p>
            </div>
          ) : (
            <div style={{ padding: '0' }}>
              {tasks.slice(0, 8).map((task, i) => (
                <div key={task.id} style={{
                  display: 'flex', alignItems: 'center', gap: '12px',
                  padding: '11px 20px',
                  borderBottom: i < Math.min(tasks.length, 8) - 1 ? '1px solid var(--border-light)' : 'none',
                  transition: 'background var(--t-fast)',
                  cursor: 'pointer',
                }}
                  onMouseEnter={e => e.currentTarget.style.background = 'var(--bg-hover)'}
                  onMouseLeave={e => e.currentTarget.style.background = 'transparent'}
                >
                  <StatusBadge status={task.status} />
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <div style={{ fontSize: '13.5px', fontWeight: 500, color: 'var(--text-primary)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{task.title}</div>
                    <div style={{ fontSize: '11.5px', color: 'var(--text-subtle)', marginTop: '1px' }}>{task.projectName}</div>
                  </div>
                  {task.dueDate && (
                    <span style={{ fontSize: '11.5px', color: new Date(task.dueDate) < new Date() ? 'var(--red-500)' : 'var(--text-subtle)', whiteSpace: 'nowrap', display: 'flex', alignItems: 'center', gap: '3px', flexShrink: 0 }}>
                      <Clock size={11} />{formatDate(task.dueDate)}
                    </span>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>

        {/* Workspaces */}
        <div className="card">
          <div className="card-header">
            <div>
              <h3 style={{ fontSize: '15px', fontWeight: 600 }}>Workspaces</h3>
              <div style={{ fontSize: '12px', color: 'var(--text-subtle)', marginTop: '2px' }}>{workspaces.length} active</div>
            </div>
            <Link to="/workspaces" className="btn btn-subtle btn-sm" style={{ color: 'var(--text-link)', fontSize: '12px', display: 'flex', alignItems: 'center', gap: '4px' }}>
              View all <ArrowRight size={12} />
            </Link>
          </div>
          {workspaces.length === 0 ? (
            <div className="empty-state" style={{ padding: '40px 20px' }}>
              <div className="empty-state-icon"><Briefcase size={22} /></div>
              <p className="empty-state-title" style={{ fontSize: '14px' }}>No workspaces</p>
              <Link to="/workspaces" className="btn btn-primary btn-sm"><Plus size={12}/> Create workspace</Link>
            </div>
          ) : (
            <div style={{ padding: '8px' }}>
              {workspaces.map(ws => (
                <Link to={`/workspaces/${ws.id}/projects`} key={ws.id} style={{
                  display: 'flex', alignItems: 'center', gap: '12px',
                  padding: '10px 12px', borderRadius: 'var(--r-md)',
                  transition: 'background var(--t-fast)', textDecoration: 'none',
                }}
                  onMouseEnter={e => e.currentTarget.style.background = 'var(--bg-hover)'}
                  onMouseLeave={e => e.currentTarget.style.background = 'transparent'}
                >
                  <div style={{ width: '36px', height: '36px', borderRadius: 'var(--r-md)', background: 'var(--blue-600)', display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
                    <FolderKanban size={16} color="white" />
                  </div>
                  <div style={{ flex: 1, minWidth: 0 }}>
                    <div style={{ fontSize: '13.5px', fontWeight: 500, color: 'var(--text-primary)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>{ws.name}</div>
                    <div style={{ fontSize: '11.5px', color: 'var(--text-subtle)' }}>{ws.memberCount} member{ws.memberCount !== 1 ? 's' : ''} · {timeAgo(ws.createdAt)}</div>
                  </div>
                  <ArrowRight size={14} style={{ color: 'var(--gray-300)', flexShrink: 0 }} />
                </Link>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
