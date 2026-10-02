import { create } from 'zustand'

const useAuthStore = create((set, get) => ({
  user: JSON.parse(localStorage.getItem('user') || 'null'),
  accessToken: localStorage.getItem('accessToken') || null,
  refreshToken: localStorage.getItem('refreshToken') || null,
  isAuthenticated: !!localStorage.getItem('accessToken'),

  login: (data) => {
    localStorage.setItem('accessToken', data.accessToken)
    localStorage.setItem('refreshToken', data.refreshToken)
    localStorage.setItem('user', JSON.stringify({
      id: data.userId, name: data.name, email: data.email, role: data.role,
    }))
    set({
      user: { id: data.userId, name: data.name, email: data.email, role: data.role },
      accessToken: data.accessToken,
      refreshToken: data.refreshToken,
      isAuthenticated: true,
    })
  },

  logout: () => {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('user')
    set({ user: null, accessToken: null, refreshToken: null, isAuthenticated: false })
  },

  updateUser: (updates) => {
    const updated = { ...get().user, ...updates }
    localStorage.setItem('user', JSON.stringify(updated))
    set({ user: updated })
  },
}))

export default useAuthStore
