import {
  useMemo,
  useState,
  type ReactNode,
} from 'react'

import { login } from '../api/authApi'
import type { AuthUser } from '../types/auth'
import { clearStoredAuth, getStoredAuth, storeAuth } from './authStorage'
import { AuthContext, type AuthContextValue } from './AuthContextDefinition'

interface AuthProviderProps {
  children: ReactNode
}

export function AuthProvider({ children }: AuthProviderProps) {
  const [user, setUser] = useState<AuthUser | null>(() => getStoredAuth())

  const loginUser = async (
    email: string,
    password: string,
  ): Promise<void> => {
    const response = await login({ email, password })
    const authenticatedUser: AuthUser = {
      ...response,
      role: response.role === 'ORGANIZER'
        ? 'ROLE_ORGANIZER'
        : 'ROLE_USER',
    }

    storeAuth(response.token, authenticatedUser)
    setUser(authenticatedUser)
  }

  const logout = (): void => {
    clearStoredAuth()
    setUser(null)
  }

  const value = useMemo<AuthContextValue>(
    () => ({
      user,
      isAuthenticated: user !== null,
      loginUser,
      logout,
    }),
    [user],
  )

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  )
}
