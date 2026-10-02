import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { notificationApi } from '../api'
import { LoadingPage } from '../components/Spinner'
import { timeAgo } from '../utils/helpers'
import toast from 'react-hot-toast'
import { Bell, CheckCheck, Info, CheckCircle, MessageSquare } from 'lucide-react'
import { Link } from 'react-router-dom'

const TYPE_ICONS = {
  TASK_ASSIGNED: CheckCircle,
  TASK_STATUS_CHANGED: Info,
  TASK_COMMENT_ADDED: MessageSquare,
}

export default function NotificationsPage() {
  const qc = useQueryClient()

  const { data, isLoading } = useQuery({
    queryKey: ['notifications'],
    queryFn: () => notificationApi.getAll(),
  })

  const markAllRead = useMutation({
    mutationFn: () => notificationApi.markAllRead(),
    onSuccess: () => { qc.invalidateQueries({ queryKey: ['notifications'] }); toast.success('All marked as read') },
  })

  const markRead = useMutation({
    mutationFn: (id) => notificationApi.markRead(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['notifications'] }),
  })

  const notifications = data?.data?.data?.content || []
  const unread = notifications.filter(n => !n.read).length

  if (isLoading) return <LoadingPage />

  return (
    <div style={{ maxWidth: '780px' }}>
      <div className="page-header">
        <div>
          <div className="page-breadcrumb">
            <Link to="/dashboard">Dashboard</Link>
            <span>/</span>
            <span>Notifications</span>
          </div>
          <h1 className="page-title">Notifications</h1>
          <p className="page-subtitle">{unread > 0 ? `${unread} unread alert${unread > 1 ? 's' : ''}` : 'All caught up'}</p>
        </div>
        {unread > 0 && (
          <button className="btn btn-default" onClick={() => markAllRead.mutate()}>
            <CheckCheck size={15} /> Mark all as read
          </button>
        )}
      </div>

      {notifications.length === 0 ? (
        <div className="empty-state">
          <div className="empty-state-icon"><Bell size={28} /></div>
          <h3 className="empty-state-title">No notifications</h3>
          <p className="empty-state-desc">You'll be notified here when team members mention you, assign tasks, or change statuses.</p>
        </div>
      ) : (
        <div className="card" style={{ overflow: 'hidden' }}>
          <div style={{ display: 'flex', flexDirection: 'column' }}>
            {notifications.map((notif, idx) => {
              const Icon = TYPE_ICONS[notif.type] || Bell
              return (
                <div
                  key={notif.id}
                  onClick={() => !notif.read && markRead.mutate(notif.id)}
                  style={{
                    display: 'flex',
                    alignItems: 'flex-start',
                    gap: '14px',
                    padding: '14px 18px',
                    background: notif.read ? 'transparent' : 'var(--blue-50)',
                    borderBottom: idx === notifications.length - 1 ? 'none' : '1px solid var(--border-light)',
                    cursor: notif.read ? 'default' : 'pointer',
                    transition: 'background var(--t-fast)',
                  }}
                >
                  <div style={{
                    width: '32px',
                    height: '32px',
                    borderRadius: 'var(--r-md)',
                    background: notif.read ? 'var(--gray-50)' : '#FFFFFF',
                    border: '1px solid var(--border-light)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    flexShrink: 0,
                    color: notif.read ? 'var(--text-subtle)' : 'var(--blue-600)',
                  }}>
                    <Icon size={15} />
                  </div>

                  <div style={{ flex: 1 }}>
                    <div style={{
                      fontSize: '13.5px',
                      color: 'var(--text-primary)',
                      fontWeight: notif.read ? 400 : 600,
                      lineHeight: 1.45
                    }}>
                      {notif.message}
                    </div>
                    <div style={{ fontSize: '11.5px', color: 'var(--text-subtle)', marginTop: '4px' }}>
                      {timeAgo(notif.createdAt)}
                    </div>
                  </div>

                  {!notif.read && (
                    <div style={{
                      width: '8px',
                      height: '8px',
                      borderRadius: '50%',
                      background: 'var(--blue-500)',
                      flexShrink: 0,
                      marginTop: '6px'
                    }} />
                  )}
                </div>
              )
            })}
          </div>
        </div>
      )}
    </div>
  )
}
