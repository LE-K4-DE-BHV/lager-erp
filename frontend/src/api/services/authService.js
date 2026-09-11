import api from '../axios'

export const authService = {
  login: (username, password) => {
    const params = new URLSearchParams({ username, password })
    return api.post('/auth/login', params, {
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    })
  },
  logout: () => api.post('/auth/logout'),
  me:    () => api.get('/auth/me'),
}
