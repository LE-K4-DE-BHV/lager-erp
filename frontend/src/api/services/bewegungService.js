import api from '../axios'

export const bewegungService = {
  bucheEingang:          (data)          => api.post('/bewegungen/eingang', data),
  schliesseBestellung:   (transaktionId) => api.post(`/bewegungen/eingang/${transaktionId}/bestellung-abschliessen`),
  bucheAusgang:          (data)          => api.post('/bewegungen/ausgang', data),
}
