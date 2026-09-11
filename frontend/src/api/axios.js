import axios from 'axios'
import router from '../router'

const apiClient = axios.create({
  baseURL: '/api/v1',
  withCredentials: true,
})

apiClient.interceptors.response.use(
  response => response,
  error => {
    if (error.response?.status === 401) {
      router.push('/login')
    }
    return Promise.reject(error)
  }
)

export default apiClient
