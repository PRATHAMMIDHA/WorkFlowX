import { getStatusClass, getPriorityClass, formatStatus } from '../utils/helpers'

export function StatusBadge({ status }) {
  return <span className={`badge ${getStatusClass(status)}`}>{formatStatus(status)}</span>
}

export function PriorityBadge({ priority }) {
  if (!priority) return null
  return (
    <span style={{ display: 'inline-flex', alignItems: 'center', gap: '5px', fontSize: '12px', fontWeight: 500 }}>
      <span className={`priority-dot ${priority}`} />
      {priority.charAt(0) + priority.slice(1).toLowerCase()}
    </span>
  )
}
