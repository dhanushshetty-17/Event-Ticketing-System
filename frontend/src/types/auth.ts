export type BackendUserRole = 'USER' | 'ORGANIZER'
export type UserRole = 'ROLE_USER' | 'ROLE_ORGANIZER'

export interface LoginRequest {
  email: string
  password: string
}

export interface LoginResponse {
  token: string
  tokenType: string
  userId: string
  email: string
  role: BackendUserRole
}

export interface RegistrationRequest {
  email: string
  password: string
}

export interface UserResponse {
  id: string
  email: string
  role: BackendUserRole
}

export interface AuthUser extends Omit<LoginResponse, 'role'> {
  role: UserRole
}