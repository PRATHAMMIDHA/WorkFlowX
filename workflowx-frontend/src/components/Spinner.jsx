export default function Spinner({ size = '' }) {
  return <div className={`spinner ${size}`} role="status" aria-label="Loading" />
}

export function LoadingPage() {
  return (
    <div className="loading-overlay">
      <Spinner size="spinner-lg" />
      <span style={{ fontSize: '13px', color: 'var(--text-subtle)' }}>Loading…</span>
    </div>
  )
}
