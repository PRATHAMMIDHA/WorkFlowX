import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  headers: { 'Content-Type': 'application/json' },
})

// Attach token to every request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('accessToken')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

// Handle 401 → logout
api.interceptors.response.use(
  (res) => res,
  async (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem('user')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export default api

// ── Auth ────────────────────────────────────────────────────
export const authApi = {
  register: (data) => api.post('/auth/register', data),
  login: (data) => api.post('/auth/login', data),
  logout: (refreshToken) => api.post('/auth/logout', { refreshToken }),
}

// ── Users ───────────────────────────────────────────────────
export const userApi = {
  getMe: () => api.get('/users/me'),
  updateProfile: (data) => api.put('/users/me', data),
  changePassword: (data) => api.put('/users/me/password', data),
  getUser: (id) => api.get(`/users/${id}`),
}

// ── Workspaces ───────────────────────────────────────────────
export const workspaceApi = {
  create: (data) => api.post('/workspaces', data),
  getAll: () => api.get('/workspaces'),
  getById: (id) => api.get(`/workspaces/${id}`),
  update: (id, data) => api.put(`/workspaces/${id}`, data),
  delete: (id) => api.delete(`/workspaces/${id}`),
  getMembers: (id) => api.get(`/workspaces/${id}/members`),
  addMember: (id, data) => api.post(`/workspaces/${id}/members`, data),
  removeMember: (id, userId) => api.delete(`/workspaces/${id}/members/${userId}`),
}

// ── Projects ─────────────────────────────────────────────────
export const projectApi = {
  create: (data) => api.post('/projects', data),
  getByWorkspace: (workspaceId) => api.get(`/projects?workspaceId=${workspaceId}`),
  getById: (id) => api.get(`/projects/${id}`),
  update: (id, data) => api.put(`/projects/${id}`, data),
  delete: (id) => api.delete(`/projects/${id}`),
  getStats: (id) => api.get(`/projects/${id}/stats`),
  getMembers: (id) => api.get(`/projects/${id}/members`),
  addMember: (id, userId) => api.post(`/projects/${id}/members/${userId}`),
  removeMember: (id, userId) => api.delete(`/projects/${id}/members/${userId}`),
}

// ── Sprints ───────────────────────────────────────────────────
export const sprintApi = {
  create: (data) => api.post('/sprints', data),
  getByProject: (projectId) => api.get(`/sprints?projectId=${projectId}`),
  getById: (id) => api.get(`/sprints/${id}`),
  update: (id, data) => api.put(`/sprints/${id}`, data),
  updateStatus: (id, status) => api.patch(`/sprints/${id}/status?status=${status}`),
  delete: (id) => api.delete(`/sprints/${id}`),
}

// ── Tasks ─────────────────────────────────────────────────────
export const taskApi = {
  create: (data) => api.post('/tasks', data),
  getAll: (params) => api.get('/tasks', { params }),
  getById: (id) => api.get(`/tasks/${id}`),
  update: (id, data) => api.put(`/tasks/${id}`, data),
  updateStatus: (id, status) => api.patch(`/tasks/${id}/status?status=${status}`),
  delete: (id) => api.delete(`/tasks/${id}`),
}

// ── Comments ──────────────────────────────────────────────────
export const commentApi = {
  create: (taskId, data) => api.post(`/tasks/${taskId}/comments`, data),
  getByTask: (taskId) => api.get(`/tasks/${taskId}/comments`),
  update: (taskId, commentId, data) => api.put(`/tasks/${taskId}/comments/${commentId}`, data),
  delete: (taskId, commentId) => api.delete(`/tasks/${taskId}/comments/${commentId}`),
}

// ── Teams ─────────────────────────────────────────────────────
export const teamApi = {
  create: (data) => api.post('/teams', data),
  getByWorkspace: (workspaceId) => api.get(`/teams?workspaceId=${workspaceId}`),
  getById: (id) => api.get(`/teams/${id}`),
  update: (id, data) => api.put(`/teams/${id}`, data),
  delete: (id) => api.delete(`/teams/${id}`),
  getMembers: (id) => api.get(`/teams/${id}/members`),
  addMember: (id, userId) => api.post(`/teams/${id}/members/${userId}`),
  removeMember: (id, userId) => api.delete(`/teams/${id}/members/${userId}`),
}

// ── Notifications ─────────────────────────────────────────────
export const notificationApi = {
  getAll: (page = 0, size = 20) => api.get(`/notifications?page=${page}&size=${size}`),
  getUnreadCount: () => api.get('/notifications/unread-count'),
  markAllRead: () => api.patch('/notifications/mark-all-read'),
  markRead: (id) => api.patch(`/notifications/${id}/read`),
}
