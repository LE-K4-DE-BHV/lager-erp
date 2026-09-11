import api from '../axios'

export const artikelService = {
  getAll:        ()          => api.get('/artikel'),
  getById:       (id)        => api.get(`/artikel/${id}`),
  create:        (data)      => api.post('/artikel', data),
  update:        (id, data)  => api.put(`/artikel/${id}`, data),
  deaktivieren:  (id)        => api.put(`/artikel/${id}/deaktivieren`),
}
