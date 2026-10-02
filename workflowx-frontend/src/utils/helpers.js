// Utility helpers

export const getInitials = (name) => {
  if (!name) return '?'
  return name.split(' ').map(n => n[0]).join('').toUpperCase().slice(0, 2)
}

export const getStatusClass = (status) => {
  const map = {
    BACKLOG: 'badge-backlog', TODO: 'badge-todo',
    IN_PROGRESS: 'badge-progress', CODE_REVIEW: 'badge-review', DONE: 'badge-done',
    PLANNING: 'badge-planning', ACTIVE: 'badge-active', COMPLETED: 'badge-completed',
    PLANNED: 'badge-planned', ON_HOLD: 'badge-on-hold', ARCHIVED: 'badge-archived',
  }
  return map[status] || 'badge-backlog'
}

export const getPriorityClass = (priority) => {
  const map = {
    LOW: 'badge-priority-low', MEDIUM: 'badge-priority-medium',
    HIGH: 'badge-priority-high', CRITICAL: 'badge-priority-critical',
  }
  return map[priority] || 'badge-priority-medium'
}

export const formatStatus = (status) => {
  const map = {
    BACKLOG: 'Backlog', TODO: 'To Do', IN_PROGRESS: 'In Progress',
    CODE_REVIEW: 'Review', DONE: 'Done', PLANNING: 'Planning',
    ACTIVE: 'Active', COMPLETED: 'Completed', PLANNED: 'Planned',
    ON_HOLD: 'On Hold', ARCHIVED: 'Archived',
  }
  return map[status] || status
}

export const formatDate = (date) => {
  if (!date) return '—'
  return new Date(date).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
}

export const timeAgo = (date) => {
  if (!date) return ''
  const seconds = Math.floor((Date.now() - new Date(date)) / 1000)
  if (seconds < 60) return 'just now'
  const minutes = Math.floor(seconds / 60)
  if (minutes < 60) return `${minutes}m ago`
  const hours = Math.floor(minutes / 60)
  if (hours < 24) return `${hours}h ago`
  const days = Math.floor(hours / 24)
  if (days < 7) return `${days}d ago`
  return formatDate(date)
}

export const TASK_STATUSES = ['BACKLOG', 'TODO', 'IN_PROGRESS', 'CODE_REVIEW', 'DONE']
export const TASK_PRIORITIES = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']
export const PROJECT_STATUSES = ['PLANNING', 'ACTIVE', 'ON_HOLD', 'COMPLETED', 'ARCHIVED']
export const SPRINT_STATUSES = ['PLANNED', 'ACTIVE', 'COMPLETED']

export const KANBAN_COLUMNS = [
  { id: 'BACKLOG', label: 'Backlog', color: '#64748b' },
  { id: 'TODO', label: 'To Do', color: '#3b82f6' },
  { id: 'IN_PROGRESS', label: 'In Progress', color: '#f59e0b' },
  { id: 'CODE_REVIEW', label: 'Review', color: '#8b5cf6' },
  { id: 'DONE', label: 'Done', color: '#10b981' },
]

export const handleApiError = (error) => {
  if (error?.response?.data?.message) return error.response.data.message
  if (error?.response?.data?.validationErrors) {
    const errs = error.response.data.validationErrors
    return Object.values(errs)[0] || 'Validation failed'
  }
  return error?.message || 'Something went wrong'
}
