import { getInitials } from '../utils/helpers'

export default function Avatar({ name, src, size = '' }) {
  return (
    <div className={`avatar ${size}`} title={name}>
      {src ? <img src={src} alt={name} /> : getInitials(name)}
    </div>
  )
}
