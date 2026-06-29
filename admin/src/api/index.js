import axios from 'axios'

const api = axios.create({
  baseURL: '/api/v1/admin',
  timeout: 10000,
})

api.interceptors.request.use(config => {
  const token = localStorage.getItem('admin_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  res => res.data,
  err => {
    if (err.response?.status === 401) {
      localStorage.removeItem('admin_token')
      window.location.hash = '#/login'
    }
    return Promise.reject(err)
  }
)

export default api
