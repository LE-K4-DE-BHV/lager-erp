import api from '../axios'

export const lieferantenService = {
  getAll:   ()          => api.get('/lieferanten'),
  getById:  (id)        => api.get(`/lieferanten/${id}`),
  update:   (id, data)  => api.put(`/lieferanten/${id}`, data),
}
