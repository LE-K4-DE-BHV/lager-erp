import api from '../axios'

export const dashboardService = {
  getKpis:        ()           => api.get('/dashboard/kpis'),
  getVorschlaege: ()           => api.get('/dashboard/vorschlaege'),
  triggerReorder: ()           => api.post('/reorder/trigger'),
  schnellbestellen: (vorschlagId) =>
    api.post(`/bestellungen/schnell/${vorschlagId}`),
  bestellungAufgeben: (vorschlagId, payload) =>
    api.post(`/bestellungen/bearbeitet/${vorschlagId}`, payload),
  ignorieren: (vorschlagId)    => api.put(`/bestellvorschlaege/${vorschlagId}/ignorieren`),
}
