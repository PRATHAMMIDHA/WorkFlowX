import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { taskApi } from '../api'
import { LoadingPage } from '../components/Spinner'
import { StatusBadge, PriorityBadge } from '../components/Badge'
import useAuthStore from '../store/authStore'
import { handleApiError, formatDate, TASK_STATUSES, TASK_PRIORITIES } from '../utils/helpers'
import toast from 'react-hot-toast'
import { CheckSquare, Clock, Filter, RotateCcw, Calendar, Folder } from 'lucide-react'
import { Link } from 'react-router-dom'

export default function TasksPage() {
  const user = useAuthStore(s => s.user)
  const qc = useQueryClient()
  const [filters, setFilters] = useState({ status: '', priority: '', keyword: '' })

  const { data, isLoading } = useQuery({
    queryKey: ['myTasks', filters],
    queryFn: () => taskApi.getAll({ assigneeId: user?.id, ...filters, size: 100 }),
  })

  const updateStatus = useMutation({
    mutationFn: ({ id, status }) => taskApi.updateStatus(id, status),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['myTasks'] }); toast.success('Status updated') },
    onError: (err) => toast.error(handleApiError(err)),
  })

  const tasks = data?.data?.data?.content || []
  const total = data?.data?.data?.totalElements || 0

  const resetFilters = () => setFilters({ status: '', priority: '', keyword: '' })
  const hasActiveFilters = Boolean(filters.status || filters.priority || filters.keyword)

  if (isLoading) return <LoadingPage />

  return (
    <div>
      <div className="page-header">
        <div>
          <div className="page-breadcrumb">
            <Link to="/dashboard">Dashboard</Link>
            <span>/</span>
            <span>My Work</span>
          </div>
          <h1 className="page-title">My Tasks</h1>
          <p className="page-subtitle">{total} assigned issue{total !== 1 ? 's' : ''} across all projects</p>
        </div>
      </div>

      {/* Filter Toolbar */}
      <div className="card" style={{ padding: '12px 16px', marginBottom: '20px', display: 'flex', gap: '12px', flexWrap: 'wrap', alignItems: 'center' }}>
        <div className="search-wrapper">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><circle cx="11" cy="11" r="8"/><path d="m21 21-4.35-4.35"/></svg>
          <input
            type="text"
            className="search-input"
            placeholder="Search by title or summary…"
            value={filters.keyword}
            onChange={e => setFilters(f => ({ ...f, keyword: e.target.value }))}
            style={{ width: '240px' }}
          />
        </div>

        <select
          className="input"
          style={{ width: 'auto', minWidth: '130px', padding: '6px 28px 6px 10px', fontSize: '13px' }}
          value={filters.status}
          onChange={e => setFilters(f => ({ ...f, status: e.target.value }))}
        >
          <option value="">Status: All</option>
          {TASK_STATUSES.map(s => <option key={s} value={s}>{s.replace('_', ' ')}</option>)}
        </select>

        <select
          className="input"
          style={{ width: 'auto', minWidth: '130px', padding: '6px 28px 6px 10px', fontSize: '13px' }}
          value={filters.priority}
          onChange={e => setFilters(f => ({ ...f, priority: e.target.value }))}
        >
          <option value="">Priority: All</option>
          {TASK_PRIORITIES.map(p => <option key={p} value={p}>{p}</option>)}
        </select>

        {hasActiveFilters && (
          <button className="btn btn-subtle btn-sm" onClick={resetFilters} style={{ marginLeft: 'auto', color: 'var(--blue-600)' }}>
            <RotateCcw size={12} /> Clear filters
          </button>
        )}
      </div>

      {tasks.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-icon"><CheckSquare size={28} /></div>
          <h3 className="empty-state-title">No issues found</h3>
          <p className="empty-state-desc">
            {hasActiveFilters ? 'No tasks matched your active filter criteria. Try clearing them.' : 'You have no issues assigned to you right now.'}
          </p>
          {hasActiveFilters && (
            <button className="btn btn-default btn-sm" onClick={resetFilters}>
              Reset Filters
            </button>
          )}
        </div>
      ) : (
        <div className="table-wrapper">
          <table className="table">
            <thead>
              <tr>
                <th style={{ width: '38%' }}>Issue Key & Summary</th>
                <th>Project</th>
                <th>Priority</th>
                <th>Status</th>
                <th>Sprint</th>
                <th>Due Date</th>
                <th style={{ textAlign: 'right' }}>Update</th>
              </tr>
            </thead>
            <tbody>
              {tasks.map(task => {
                const isOverdue = task.dueDate && new Date(task.dueDate) < new Date() && task.status !== 'DONE'
                return (
                  <tr key={task.id}>
                    <td>
                      <div>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '2px' }}>
                          <span style={{ fontSize: '11px', fontWeight: 700, color: 'var(--blue-600)', letterSpacing: '0.3px' }}>
                            TSK-{task.id}
                          </span>
                          <span style={{ fontWeight: 600, color: 'var(--text-primary)', fontSize: '13.5px' }}>
                            {task.title}
                          </span>
                        </div>
                        {task.description && (
                          <div style={{ fontSize: '12px', color: 'var(--text-subtle)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', maxWidth: '340px' }}>
                            {task.description}
                          </div>
                        )}
                      </div>
                    </td>
                    <td>
                      <span style={{ display: 'inline-flex', alignItems: 'center', gap: '5px', fontSize: '12.5px', color: 'var(--text-secondary)' }}>
                        <Folder size={13} style={{ color: 'var(--gray-400)' }} />
                        {task.projectName}
                      </span>
                    </td>
                    <td>
                      <PriorityBadge priority={task.priority} />
                    </td>
                    <td>
                      <StatusBadge status={task.status} />
                    </td>
                    <td>
                      <span style={{ fontSize: '12.5px', color: task.sprintName ? 'var(--text-secondary)' : 'var(--text-disabled)' }}>
                        {task.sprintName || 'None'}
                      </span>
                    </td>
                    <td>
                      {task.dueDate ? (
                        <span style={{
                          fontSize: '12px',
                          display: 'inline-flex',
                          alignItems: 'center',
                          gap: '4px',
                          color: isOverdue ? 'var(--red-500)' : 'var(--text-secondary)',
                          fontWeight: isOverdue ? 600 : 400,
                          background: isOverdue ? 'var(--red-100)' : 'transparent',
                          padding: isOverdue ? '2px 6px' : '0',
                          borderRadius: 'var(--r-sm)'
                        }}>
                          <Clock size={12} /> {formatDate(task.dueDate)}
                        </span>
                      ) : (
                        <span style={{ color: 'var(--text-disabled)', fontSize: '12px' }}>—</span>
                      )}
                    </td>
                    <td style={{ textAlign: 'right' }}>
                      <select
                        className="input"
                        style={{
                          width: 'auto',
                          fontSize: '12px',
                          padding: '4px 24px 4px 8px',
                          height: '28px',
                          borderRadius: 'var(--r-sm)',
                        }}
                        value={task.status}
                        onChange={e => updateStatus.mutate({ id: task.id, status: e.target.value })}
                      >
                        {TASK_STATUSES.map(s => <option key={s} value={s}>{s.replace('_', ' ')}</option>)}
                      </select>
                    </td>
                  </tr>
                )
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
