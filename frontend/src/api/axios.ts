import axios from 'axios'

import { clearStoredAuth } from '../context/authStorage'

export const AUTH_SESSION_EXPIRED_EVENT = 'auth:session-expired'
export const AUTH_FORBIDDEN_EVENT = 'auth:forbidden'

function dispatchWindowEvent(name: string, detail: Record<string, string>) {
  if (typeof window !== 'undefined') {
    window.dispatchEvent(new CustomEvent(name, { detail }))
  }
}

const apiClient = axios.create({
  baseURL:
    import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
  headers: {
    'Content-Type': 'application/json',
  },
})

apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken')

    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }

    return config
  },
  (error) => Promise.reject(error),
)

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      clearStoredAuth()
      dispatchWindowEvent(AUTH_SESSION_EXPIRED_EVENT, {
        returnTo: `${window.location.pathname}${window.location.search}${window.location.hash}`,
      })
    } else if (error.response?.status === 403) {
      dispatchWindowEvent(AUTH_FORBIDDEN_EVENT, {
        message: 'You do not have permission to perform this action.',
      })
    }

    return Promise.reject(error)
  },
)

export default apiClient