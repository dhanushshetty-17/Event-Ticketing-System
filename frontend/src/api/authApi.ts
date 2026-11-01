import apiClient from './axios'
import type {
  LoginRequest,
  LoginResponse,
  RegistrationRequest,
  UserResponse,
} from '../types/auth'

export type {
  LoginRequest,
  LoginResponse,
  RegistrationRequest,
  UserResponse,
} from '../types/auth'

export const login = async (
  credentials: LoginRequest,
): Promise<LoginResponse> => {
  const response = await apiClient.post<LoginResponse>(
    '/auth/login',
    credentials,
  )

  return response.data
}

export const register = async (
  credentials: RegistrationRequest,
): Promise<UserResponse> => {
  const response = await apiClient.post<UserResponse>(
    '/users',
    credentials,
  )

  return response.data
}