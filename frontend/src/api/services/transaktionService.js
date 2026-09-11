import api from '../axios'

export const transaktionService = {
  getAll: (params) => api.get('/transaktionen', { params }),
}

export const bestellungService = {
  getAll:   ()   => api.get('/bestellungen'),
  getById:  (id) => api.get(`/bestellungen/${id}`),
}
