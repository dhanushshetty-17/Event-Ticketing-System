import apiClient from './axios'

export interface LoginRequest {
  email: string
  password: string
}

export interface LoginResponse {
  token: string
  tokenType: string
  userId: string
  email: string
  role: 'USER' | 'ORGANIZER'
}

export const login = async (
  credentials: LoginRequest,
): Promise<LoginResponse> => {
  const response = await apiClient.post<LoginResponse>(
    '/auth/login',
    credentials,
  )

  return response.data
}